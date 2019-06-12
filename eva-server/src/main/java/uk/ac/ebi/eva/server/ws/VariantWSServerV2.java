/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.eva.server.ws;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.eva.commons.core.models.Annotation;
import uk.ac.ebi.eva.commons.core.models.VariantSource;
import uk.ac.ebi.eva.commons.core.models.ws.VariantSourceEntryWithSampleNames;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ebi.eva.commons.core.models.AnnotationMetadata;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.filter.FilterBuilder;
import uk.ac.ebi.eva.commons.mongodb.filter.VariantRepositoryFilter;
import uk.ac.ebi.eva.commons.mongodb.services.AnnotationMetadataNotFoundException;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.server.Utils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping(value = "/v2/variants", produces = "application/json")
@Api(tags = {"variants"})
public class VariantWSServerV2 extends EvaWSServer {

    @Autowired
    private VariantWithSamplesAndAnnotationsService service;

    @GetMapping(value = "/{variantId}/info")
    public QueryResponse getCoreInfo(@PathVariable("variantId") String variantId,
                                     @RequestParam(name = "studies", required = false) List<String> studies,
                                     @RequestParam(name = "species") String species,
                                     @RequestParam(name = "annot-ct", required = false) List<String> consequenceType,
                                     @RequestParam(name = "maf", required = false) String maf,
                                     @RequestParam(name = "polyphen", required = false) String polyphenScore,
                                     @RequestParam(name = "sift", required = false) String siftScore,
                                     @RequestParam(name = "exclude", required = false) List<String> exclude,
                                     @RequestParam(name = "annot-vep-version", required = false) String annotationVepVersion,
                                     @RequestParam(name = "annot-vep-cache-version", required = false) String annotationVepCacheVersion,
                                     HttpServletResponse response)
            throws IOException {
        initializeQuery();

        String errorMessage = checkErrorHelper(annotationVepVersion, annotationVepCacheVersion, species, exclude);
        if (errorMessage != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(errorMessage);
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));

        List<VariantWithSamplesAndAnnotation> variantEntities;
        Long numTotalResults;

        try {
            if (variantId.contains(":")) {
                variantEntities = getVariantEntitiesByParams(variantId, annotationVepVersion,
                        annotationVepCacheVersion);
                numTotalResults = (long) variantEntities.size();
            } else {

                List<VariantRepositoryFilter> filters = new FilterBuilder()
                        .getVariantEntityRepositoryFilters(maf, polyphenScore, siftScore, studies, consequenceType);
                variantEntities = getVariantEntitiesByVariantId(exclude, annotationVepVersion, annotationVepCacheVersion,
                        variantId, filters);
                numTotalResults = service.countByIdsAndComplexFilters(Arrays.asList(variantId), filters);
            }
        } catch (AnnotationMetadataNotFoundException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(ex.getMessage());
        }

        List<VariantWithSamplesAndAnnotation> rootVariantEntities = new ArrayList<>();
        variantEntities.forEach(variantEntity -> {
            rootVariantEntities.add(new VariantWithSamplesAndAnnotation(variantEntity.getChromosome(),
                    variantEntity.getStart(), variantEntity.getEnd(), variantEntity.getReference(),
                    variantEntity.getReference(), variantEntity.getMainId()));
        });

        QueryResult<VariantWithSamplesAndAnnotation> queryResult = buildQueryResult(rootVariantEntities, numTotalResults);

        return setQueryResponse(queryResult);
    }

    private List<VariantWithSamplesAndAnnotation> getVariantEntitiesByParams(String variantId,
                                                                             String annotationVepVersion,
                                                                             String annotationVepCacheVersion) throws
            AnnotationMetadataNotFoundException {
        String[] regionId = variantId.split(":");
        String alternate = (regionId.length > 3) ? regionId[3] : null;

        AnnotationMetadata annotationMetadata = null;
        if (annotationVepVersion != null && annotationVepCacheVersion != null) {
            annotationMetadata = new AnnotationMetadata(annotationVepVersion, annotationVepCacheVersion);
        }

        if (alternate != null) {
            return service.findByChromosomeAndStartAndReferenceAndAlternate(regionId[0], Integer.parseInt(regionId[1]),
                    regionId[2], alternate, annotationMetadata);
        } else {
            return service.findByChromosomeAndStartAndReference(regionId[0], Integer.parseInt(regionId[1]),
                    regionId[2], annotationMetadata);
        }
    }

    private List<VariantWithSamplesAndAnnotation> getVariantEntitiesByVariantId(List<String> exclude,
                                                                                String annotationVepVersion,
                                                                                String annotationVepCacheVersion,
                                                                                String variantId,
                                                                                List<VariantRepositoryFilter> filters)
            throws AnnotationMetadataNotFoundException {

        List<String> excludeMapped = new ArrayList<>();
        if (exclude != null && !exclude.isEmpty()) {
            for (String e : exclude) {
                String docPath = Utils.getApiToMongoDocNameMap().get(e);
                excludeMapped.add(docPath);
            }
        }

        AnnotationMetadata annotationMetadata = null;
        if (annotationVepVersion != null && annotationVepCacheVersion != null) {
            annotationMetadata = new AnnotationMetadata(annotationVepVersion, annotationVepCacheVersion);
        }

        return service.findByIdsAndComplexFilters(Arrays.asList(variantId), filters, annotationMetadata, excludeMapped,
                Utils.getPageRequest(getQueryOptions()));
    }


    private String checkErrorHelper(String annotationVepVersion, String annotationVepCacheVersion, String species,
                                    List<String> exclude) {
        if (annotationVepVersion == null ^ annotationVepCacheVersion == null) {
            return "Please specify either both annotation VEP version and annotation VEP cache version, or neither";
        }

        if (species.isEmpty()) {
            return "Please specify a species";
        }

        if (exclude != null && !exclude.isEmpty()) {
            for (String e : exclude) {
                String docPath = Utils.getApiToMongoDocNameMap().get(e);
                if (docPath == null) {
                    return "Unrecognised exclude field: " + e;
                }
            }
        }
        return null;
    }

    @GetMapping(value = "/{variantId}/info/annotations")
    public QueryResponse getAnnotations(@PathVariable("variantId") String variantId,
                                        @RequestParam(name = "studies", required = false) List<String> studies,
                                        @RequestParam(name = "species") String species,
                                        @RequestParam(name = "annot-ct", required = false) List<String> consequenceType,
                                        @RequestParam(name = "maf", required = false) String maf,
                                        @RequestParam(name = "polyphen", required = false) String polyphenScore,
                                        @RequestParam(name = "sift", required = false) String siftScore,
                                        @RequestParam(name = "exclude", required = false) List<String> exclude,
                                        @RequestParam(name = "annot-vep-version", required = false) String annotationVepVersion,
                                        @RequestParam(name = "annot-vep-cache-version", required = false) String annotationVepCacheVersion,
                                        HttpServletResponse response)
            throws IOException {
        initializeQuery();

        String errorMessage = checkErrorHelper(annotationVepVersion, annotationVepCacheVersion, species, exclude);
        if (errorMessage != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(errorMessage);
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));

        List<VariantWithSamplesAndAnnotation> variantEntities;
        try {
            if (variantId.contains(":")) {
                variantEntities = getVariantEntitiesByParams(variantId, annotationVepVersion,
                        annotationVepCacheVersion);
            } else {

                List<VariantRepositoryFilter> filters = new FilterBuilder()
                        .getVariantEntityRepositoryFilters(maf, polyphenScore, siftScore, studies, consequenceType);
                variantEntities = getVariantEntitiesByVariantId(exclude, annotationVepVersion, annotationVepCacheVersion,
                        variantId, filters);
            }
        } catch (AnnotationMetadataNotFoundException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(ex.getMessage());
        }

        List<Annotation> annotations = new ArrayList<>();
        variantEntities.forEach(variantEntity -> {
            annotations.add(variantEntity.getAnnotation());
        });

        QueryResult<Annotation> queryResult = buildQueryResult(annotations, annotations.size());

        return setQueryResponse(queryResult);
    }

    @GetMapping(value = "/{variantId}/info/source-Entries")
    public QueryResponse getSourceEntries(@PathVariable("variantId") String variantId,
                                        @RequestParam(name = "studies", required = false) List<String> studies,
                                        @RequestParam(name = "species") String species,
                                        @RequestParam(name = "annot-ct", required = false) List<String> consequenceType,
                                        @RequestParam(name = "maf", required = false) String maf,
                                        @RequestParam(name = "polyphen", required = false) String polyphenScore,
                                        @RequestParam(name = "sift", required = false) String siftScore,
                                        @RequestParam(name = "exclude", required = false) List<String> exclude,
                                        @RequestParam(name = "annot-vep-version", required = false) String annotationVepVersion,
                                        @RequestParam(name = "annot-vep-cache-version", required = false) String annotationVepCacheVersion,
                                        HttpServletResponse response)
            throws IOException {
        initializeQuery();

        String errorMessage = checkErrorHelper(annotationVepVersion, annotationVepCacheVersion, species, exclude);
        if (errorMessage != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(errorMessage);
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));

        List<VariantWithSamplesAndAnnotation> variantEntities;
        try {
            if (variantId.contains(":")) {
                variantEntities = getVariantEntitiesByParams(variantId, annotationVepVersion,
                        annotationVepCacheVersion);
            } else {

                List<VariantRepositoryFilter> filters = new FilterBuilder()
                        .getVariantEntityRepositoryFilters(maf, polyphenScore, siftScore, studies, consequenceType);
                variantEntities = getVariantEntitiesByVariantId(exclude, annotationVepVersion, annotationVepCacheVersion,
                        variantId, filters);
            }
        } catch (AnnotationMetadataNotFoundException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(ex.getMessage());
        }

        //List<VariantSourceEntryWithSampleNames> variantSources = new ArrayList<>();
        Map<String,VariantSourceEntryWithSampleNames> temp = new HashMap<>();
        variantEntities.forEach(variantEntity -> {
            variantEntity.getSourceEntries().forEach(sourceEntry ->{
                //variantSources.add(sourceEntry);
                temp.put(sourceEntry.getStudyId()+"_"+sourceEntry.getFileId(), sourceEntry);
            });
        });
        List<Map<String,VariantSourceEntryWithSampleNames>> returnList = new ArrayList();
        returnList.add(temp);
        //QueryResult<VariantSourceEntryWithSampleNames> queryResult = buildQueryResult(variantSources,variantSources.size());
        QueryResult<Map<String,VariantSourceEntryWithSampleNames>> queryResult = buildQueryResult(returnList,
                returnList.get(0).size());

        return setQueryResponse(queryResult);
    }
}
