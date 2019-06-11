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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import uk.ac.ebi.eva.commons.core.models.AnnotationMetadata;
import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.filter.FilterBuilder;
import uk.ac.ebi.eva.commons.mongodb.filter.VariantRepositoryFilter;
import uk.ac.ebi.eva.commons.mongodb.services.AnnotationMetadataNotFoundException;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.server.RateLimit;
import uk.ac.ebi.eva.server.Utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/v2/segments", produces = "application/json")
@Api(tags = { "segments" })
public class RegionWSServerV2 extends EvaWSServer {

    @Autowired
    private VariantWithSamplesAndAnnotationsService service;

    protected static Logger logger = LoggerFactory.getLogger(FeatureWSServer.class);

    private static final int REGION_REQUEST_RATE_LIMIT = 5;

    public RegionWSServerV2() { }

    @RequestMapping(value = "/{regionId}/variants", method = RequestMethod.GET)
    @ResponseBody
    @RateLimit(value=REGION_REQUEST_RATE_LIMIT)
    public QueryResponse getVariantsByRegion(@PathVariable("regionId") String regionId,
                                             @RequestParam(name = "species") String species,
                                             @RequestParam(name = "studies", required = false) List<String> studies,
                                             @RequestParam(name = "annot-ct", required = false) List<String> consequenceType,
                                             @RequestParam(name = "maf", required = false) String maf,
                                             @RequestParam(name = "polyphen", required = false) String polyphenScore,
                                             @RequestParam(name = "sift", required = false) String siftScore,
                                             @RequestParam(name = "exclude", required = false) List<String> exclude,
                                             @RequestParam(name = "annot-vep-version", required = false) String annotationVepVersion,
                                             @RequestParam(name = "annot-vep-cache-version", required = false) String annotationVepCacheVersion,
                                             HttpServletResponse response,
                                             @ApiIgnore HttpServletRequest request)
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

        List<VariantRepositoryFilter> filters = new FilterBuilder()
                .getVariantEntityRepositoryFilters(maf, polyphenScore, siftScore, studies, consequenceType);
        List<Region> regions = Region.parseRegions(regionId);
        PageRequest pageRequest = Utils.getPageRequest(getQueryOptions());

        List<String> excludeMapped = new ArrayList<>();
        if (exclude != null && !exclude.isEmpty()){
            for (String e : exclude) {
                String docPath = Utils.getApiToMongoDocNameMap().get(e);
                if (docPath == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return setQueryResponse("Unrecognised exclude field: " + e);
                }
                excludeMapped.add(docPath);
            }
        }
        excludeMapped.add("ids");

        AnnotationMetadata annotationMetadata = null;
        if (annotationVepVersion != null && annotationVepCacheVersion != null) {
            annotationMetadata = new AnnotationMetadata(annotationVepVersion, annotationVepCacheVersion);
        }

        List<VariantWithSamplesAndAnnotation> variantEntities;

        try {
            variantEntities = service.findByRegionsAndComplexFilters(regions,
                    filters,
                    annotationMetadata,
                    excludeMapped,
                    pageRequest);
        } catch (AnnotationMetadataNotFoundException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(ex.getMessage());
        }

        Long numTotalResults = service.countByRegionsAndComplexFilters(regions, filters);

        List<VariantWithSamplesAndAnnotation> rootVariantEntities = new ArrayList<>();

        variantEntities.forEach(variantEntity -> {
            rootVariantEntities.add(new VariantWithSamplesAndAnnotation(variantEntity.getChromosome(),
                    variantEntity.getStart(),variantEntity.getEnd(),variantEntity.getReference(),variantEntity.getAlternate(),
                    null));
        });

        QueryResult<VariantWithSamplesAndAnnotation> queryResult = buildQueryResult(rootVariantEntities, numTotalResults);
        return setQueryResponse(queryResult);
    }

    @RequestMapping(value = "/{regionId}/variants", method = RequestMethod.OPTIONS)
    public QueryResponse getVariantsByRegion() {
        return setQueryResponse("");
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public QueryResponse getChromosomes(@RequestParam(name = "species") String species,
                                        HttpServletResponse response)
            throws IOException {
        if (species.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse("Please specify a species");
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));
        List<String> chromosomeList = new ArrayList<>(service.findDistinctChromosomes());
        QueryResult<String> queryResult = buildQueryResult(chromosomeList);
        return setQueryResponse(queryResult);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public void handleIllegalArgumentException(IllegalArgumentException ex, HttpServletResponse response)
            throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getLocalizedMessage());
    }
}
