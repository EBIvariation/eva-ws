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
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/v2/variants", produces = "application/json")
@Api(tags = {"variants"})
public class VariantWSServerV2 extends EvaWSServer {

    @Autowired
    private VariantWithSamplesAndAnnotationsService service;

    @RequestMapping(value = "/{variantId}/info", method = RequestMethod.GET)
    public QueryResponse getVariantById(@PathVariable("variantId") String variantId,
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
        return queryByCoordinatesAndAlleles(regionId[0], Integer.parseInt(regionId[1]), regionId[2], alternate,
                annotationVepVersion, annotationVepCacheVersion);
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

        AnnotationMetadata annotationMetadata = getAnnotationMetadataHelper(annotationVepVersion,
                annotationVepCacheVersion);

        return service.findByIdsAndComplexFilters(Arrays.asList(variantId), filters, annotationMetadata, excludeMapped,
                Utils.getPageRequest(getQueryOptions()));
    }

    private List<VariantWithSamplesAndAnnotation> queryByCoordinatesAndAlleles(String chromosome, long start,
                                                                               String reference, String alternate,
                                                                               String annotationVepVersion,
                                                                               String annotationVepCacheversion) throws AnnotationMetadataNotFoundException {
        AnnotationMetadata annotationMetadata = getAnnotationMetadataHelper(annotationVepVersion,
                annotationVepCacheversion);

        if (alternate != null) {
            return service.findByChromosomeAndStartAndReferenceAndAlternate(chromosome, start, reference, alternate,
                    annotationMetadata);
        } else {
            return service.findByChromosomeAndStartAndReference(chromosome, start, reference, annotationMetadata);
        }
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

    private AnnotationMetadata getAnnotationMetadataHelper(String annotationVepVersion,
                                                           String annotationVepCacheVersion) {
        if (annotationVepVersion != null && annotationVepCacheVersion != null) {
            return new AnnotationMetadata(annotationVepVersion, annotationVepCacheVersion);
        }
        return null;
    }
}
