/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014-2016 EMBL - European Bioinformatics Institute
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.eva.commons.core.models.AnnotationMetadata;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigAliasChromosome;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigNamingConvention;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.filter.FilterBuilder;
import uk.ac.ebi.eva.commons.mongodb.filter.VariantRepositoryFilter;
import uk.ac.ebi.eva.commons.mongodb.services.AnnotationMetadataNotFoundException;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.lib.utils.TaxonomyUtils;
import uk.ac.ebi.eva.server.Utils;
import uk.ac.ebi.eva.server.ws.contigalias.ContigAliasService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/v1/variants", produces = "application/json")
@Api(tags = {"variants"})
public class VariantWSServer extends EvaWSServer {

    @Autowired
    private VariantWithSamplesAndAnnotationsService service;

    @Autowired
    private ContigAliasService contigAliasService;

    @Autowired
    private TaxonomyUtils taxonomyUtils;

    protected static Logger logger = LoggerFactory.getLogger(FeatureWSServer.class);

    @RequestMapping(value = "/{variantId}/info", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "Retrieves the information about a variant", response = QueryResponse.class)
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
                                        @RequestParam(name = "contigNamingConvention", required = false) ContigNamingConvention contigNamingConvention,
                                        HttpServletResponse response)
            throws IOException {
        initializeQuery();

        if (annotationVepVersion == null ^ annotationVepCacheVersion == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse("Please specify either both annotation VEP version and annotation VEP cache version, or neither");
        }

        if (species.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse("Please specify a species");
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));

        List<VariantWithSamplesAndAnnotation> variantEntities;
        Long numTotalResults;
        QueryResult<VariantWithSamplesAndAnnotation> queryResult = new QueryResult<>();

        try {
            if (variantId.contains(":")) {
                String[] regionId = variantId.split(":");
                String alternate = (regionId.length > 3) ? regionId[3] : null;
                String[] dbNameParts = species.split("_", 2);
                Optional<String> asmAcc = taxonomyUtils.getAssemblyAccessionForAssemblyCode(dbNameParts[1]);
                if (asmAcc.isPresent()) {
                    ContigAliasChromosome contigAliasChromosome = contigAliasService.getUniqueInsdcChromosomeByName(regionId[0], asmAcc.get(),
                            contigNamingConvention);
                    if (contigAliasChromosome != null) {
                        String chromosomeInsdcAccession = contigAliasChromosome.getInsdcAccession();
                        if (contigNamingConvention == null) {
                            contigNamingConvention = contigAliasService.getMatchingContigNamingConvention(contigAliasChromosome, regionId[0]);
                        }
                        variantEntities = queryByCoordinatesAndAlleles(chromosomeInsdcAccession, Integer.parseInt(regionId[1]), regionId[2],
                                alternate, annotationVepVersion, annotationVepCacheVersion);

                        numTotalResults = (long) variantEntities.size();
                        queryResult = buildQueryResult(contigAliasService.getVariantsWithTranslatedContig(variantEntities,
                                contigAliasChromosome, contigNamingConvention), numTotalResults);
                    }
                }
            } else {
                List<VariantRepositoryFilter> filters = new FilterBuilder()
                        .getVariantEntityRepositoryFilters(maf, polyphenScore, siftScore, studies, consequenceType);

                List<String> excludeMapped = new ArrayList<>();
                if (exclude != null && !exclude.isEmpty()) {
                    for (String e : exclude) {
                        String docPath = Utils.getApiToMongoDocNameMap().get(e);
                        if (docPath == null) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            return setQueryResponse("Unrecognised exclude field: " + e);
                        }
                        excludeMapped.add(docPath);
                    }
                    // Recent versions of MongoDB do not allow projecting an embedded document with any of its fields.
                    // See https://www.mongodb.com/docs/v4.4/release-notes/4.4-compatibility/#path-collision-restrictions
                    if (excludeMapped.contains(Utils.FILES_NAME) && excludeMapped.contains(Utils.FILES_ATTRS_NAME)) {
                        excludeMapped.remove(Utils.FILES_ATTRS_NAME);
                    }
                }

                AnnotationMetadata annotationMetadata = null;
                if (annotationVepVersion != null && annotationVepCacheVersion != null) {
                    annotationMetadata = new AnnotationMetadata(annotationVepVersion, annotationVepCacheVersion);
                }

                variantEntities = service.findByIdsAndComplexFilters(Arrays.asList(variantId), filters, annotationMetadata, excludeMapped,
                        Utils.getPageRequest(getQueryOptions()));

                numTotalResults = service.countByIdsAndComplexFilters(Arrays.asList(variantId), filters);

                queryResult = buildQueryResult(contigAliasService.getVariantsWithTranslatedContig(variantEntities,
                        contigNamingConvention), numTotalResults);
            }
        } catch (AnnotationMetadataNotFoundException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(ex.getMessage());
        }

        return setQueryResponse(queryResult);
    }

    @RequestMapping(value = "/{variantIds}", method = RequestMethod.GET)
    public QueryResponse getVariantByIdList(@PathVariable("variantIds") String variantIds,
                                        @RequestParam(name = "studies", required = false) List<String> studies,
                                        @RequestParam(name = "species") String species,
                                        @RequestParam(name = "annot-ct", required = false) List<String> consequenceType,
                                        @RequestParam(name = "maf", required = false) String maf,
                                        @RequestParam(name = "polyphen", required = false) String polyphenScore,
                                        @RequestParam(name = "sift", required = false) String siftScore,
                                        @RequestParam(name = "exclude", required = false) List<String> exclude,
                                        @RequestParam(name = "annot-vep-version", required = false) String annotationVepVersion,
                                        @RequestParam(name = "annot-vep-cache-version", required = false) String annotationVepCacheVersion,
                                        @RequestParam(name="contigNamingConvention", required = false) ContigNamingConvention contigNamingConvention,
                                        HttpServletResponse response)
            throws IOException {
        initializeQuery();

        if (annotationVepVersion == null ^ annotationVepCacheVersion == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse("Please specify either both annotation VEP version and annotation VEP cache version, or neither");
        }

        if (species.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse("Please specify a species");
        }

        // Split the variants ids by ','
        List<String> variantIdsAsList = Arrays.asList(variantIds.split(","));


        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));

        List<VariantWithSamplesAndAnnotation> variantEntities;
        Long numTotalResults;

        try {
            List<VariantRepositoryFilter> filters = new FilterBuilder()
                    .getVariantEntityRepositoryFilters(maf, polyphenScore, siftScore, studies, consequenceType);

            List<String> excludeMapped = new ArrayList<>();
            if (exclude != null && !exclude.isEmpty()) {
                for (String e : exclude) {
                    String docPath = Utils.getApiToMongoDocNameMap().get(e);
                    if (docPath == null) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return setQueryResponse("Unrecognised exclude field: " + e);
                    }
                    excludeMapped.add(docPath);
                }
                // Recent versions of MongoDB do not allow projecting an embedded document with any of its fields.
                // See https://www.mongodb.com/docs/v4.4/release-notes/4.4-compatibility/#path-collision-restrictions
                if (excludeMapped.contains(Utils.FILES_NAME) && excludeMapped.contains(Utils.FILES_ATTRS_NAME)) {
                    excludeMapped.remove(Utils.FILES_ATTRS_NAME);
                }
            }

            AnnotationMetadata annotationMetadata = null;
            if (annotationVepVersion != null && annotationVepCacheVersion != null) {
                annotationMetadata = new AnnotationMetadata(annotationVepVersion, annotationVepCacheVersion);
            }

            variantEntities = service.findByIdsAndComplexFilters(variantIdsAsList, filters, annotationMetadata, excludeMapped,
                    Utils.getPageRequest(getQueryOptions()));

            numTotalResults = service.countByIdsAndComplexFilters(variantIdsAsList, filters);

        } catch (AnnotationMetadataNotFoundException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(ex.getMessage());
        }

        QueryResult<VariantWithSamplesAndAnnotation> queryResult = buildQueryResult(
                contigAliasService.getVariantsWithTranslatedContig(variantEntities, contigNamingConvention),
                numTotalResults);
        return setQueryResponse(queryResult);
    }

    private List<VariantWithSamplesAndAnnotation> queryByCoordinatesAndAlleles(String chromosome, long start,
                                                                               String reference, String alternate,
                                                                               String annotationVepVersion,
                                                                               String annotationVepCacheversion) throws AnnotationMetadataNotFoundException {
        AnnotationMetadata annotationMetadata = null;
        if (annotationVepVersion != null && annotationVepCacheversion != null) {
            annotationMetadata = new AnnotationMetadata(annotationVepVersion, annotationVepCacheversion);
        }
        if (alternate != null) {
            return service.findByChromosomeAndStartAndReferenceAndAlternate(chromosome, start, reference, alternate,
                                                                            annotationMetadata);
        } else {
            return service.findByChromosomeAndStartAndReference(chromosome, start, reference, annotationMetadata);
        }
    }

    @RequestMapping(value = "/{variantId}/exists", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "Checks if a variants exist", response = QueryResponse.class)
    public QueryResponse checkVariantExists(@PathVariable("variantId") String variantId,
                                            @RequestParam(name = "studies", required = false) List<String> studies,
                                            @RequestParam("species") String species,
                                            @RequestParam(name = "contigNamingConvention", required = false)
                                            ContigNamingConvention contigNamingConvention,
                                            HttpServletResponse response)
            throws IOException {
        initializeQuery();

        if (species.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse("Please specify a species");
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));

        List<VariantWithSamplesAndAnnotation> variantEntities = Collections.emptyList();
        Long numTotalResults;

        String invalidCoordinatesMessage =
                "Invalid position and alleles combination, please use chr:pos:ref or chr:pos:ref:alt";

        try {
            if (variantId.contains(":")) {
                String[] regionId = variantId.split(":", -1);
                if (regionId.length < 3) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return setErrorQueryResponse(invalidCoordinatesMessage);
                }

                String alternate = (regionId.length > 3) ? regionId[3] : null;

                String[] dbNameParts = species.split("_", 2);
                Optional<String> asmAcc = taxonomyUtils.getAssemblyAccessionForAssemblyCode(dbNameParts[1]);
                if (asmAcc.isPresent()) {
                    ContigAliasChromosome contigAliasChromosome = contigAliasService.getUniqueInsdcChromosomeByName(regionId[0], asmAcc.get(),
                            contigNamingConvention);
                    if (contigAliasChromosome != null) {
                        String chromosomeInsdcAccession = contigAliasChromosome.getInsdcAccession();
                        if (studies != null && !studies.isEmpty()) {
                            variantEntities = queryByCoordinatesAndAllelesAndStudyIds(chromosomeInsdcAccession, Integer.parseInt(regionId[1]),
                                    regionId[2], alternate, studies);
                        } else {
                            variantEntities = queryByCoordinatesAndAlleles(chromosomeInsdcAccession, Integer.parseInt(regionId[1]), regionId[2],
                                    alternate, null, null);
                        }
                    }
                }
            } else {
                List<VariantRepositoryFilter> filters = new FilterBuilder().withStudies(studies).build();
                variantEntities = service.findByIdsAndComplexFilters(Arrays.asList(variantId), filters, null, null,
                        Utils.getPageRequest(getQueryOptions()));
            }
        } catch (AnnotationMetadataNotFoundException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(ex.getMessage());
        }

        numTotalResults = (long) variantEntities.size();
        QueryResult queryResult = new QueryResult();
        queryResult.setResult(Arrays.asList(numTotalResults > 0));
        queryResult.setResultType(Boolean.class.getCanonicalName());
        return setQueryResponse(queryResult);
    }

    private List<VariantWithSamplesAndAnnotation> queryByCoordinatesAndAllelesAndStudyIds(String chromosome, long start,
                                                                                          String reference,
                                                                                          String alternate,
                                                                                          List<String> studyIds) throws AnnotationMetadataNotFoundException {
        if (alternate != null) {
            return service.findByChromosomeAndStartAndReferenceAndAlternateAndStudyIn(chromosome, start, reference,
                    alternate, studyIds, null);
        } else {
            return service.findByChromosomeAndStartAndReferenceAndStudyIn(chromosome, start, reference, studyIds, null);
        }
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public QueryResponse countVariants() {
        long totalNumberOfvariants = service.countTotalNumberOfVariants();
        System.out.println(totalNumberOfvariants);

        QueryResult queryResult = new QueryResult();
        queryResult.setResult(Arrays.asList(totalNumberOfvariants));
        queryResult.setResultType(Long.class.getCanonicalName());
        return setQueryResponse(queryResult);
    }


}
