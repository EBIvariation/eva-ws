/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014-2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.server.ws.ga4gh;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.filter.FilterBuilder;
import uk.ac.ebi.eva.commons.mongodb.filter.VariantRepositoryFilter;
import uk.ac.ebi.eva.commons.mongodb.services.AnnotationMetadataNotFoundException;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.models.ga4gh.GASearchVariantRequest;
import uk.ac.ebi.eva.lib.models.ga4gh.GASearchVariantsResponse;
import uk.ac.ebi.eva.lib.models.ga4gh.GAVariant;
import uk.ac.ebi.eva.lib.models.ga4gh.GAVariantFactory;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.server.Utils;
import uk.ac.ebi.eva.server.ws.EvaWSServer;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/ga4gh/variants", produces = "application/json")
@Api(tags = {"ga4gh", "variants"})
public class GA4GHVariantWSServer extends EvaWSServer {

    @Autowired
    private VariantWithSamplesAndAnnotationsService service;

    protected static Logger logger = LoggerFactory.getLogger(GA4GHVariantWSServer.class);

    public GA4GHVariantWSServer() {
    }

    /**
     * "start" and "end" are 0-based, whereas all the position stored are 1-based
     *
     * @see http://ga4gh.org/documentation/api/v0.5/ga4gh_api.html#/schema/org.ga4gh.GASearchVariantsRequest
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public GASearchVariantsResponse getVariantsByRegion(@RequestParam("referenceName") String chromosome,
                                                        @RequestParam("start") int start,
                                                        @RequestParam("end") int end,
//                                        @RequestParam("variantName") String id,
                                                        @RequestParam(name = "variantSetIds", required = false) List<String> files,
//                                        @RequestParam(name = "callSetIds", required = false) String samples,
                                                        @RequestParam(name = "pageToken", required = false) String pageToken,
                                                        @RequestParam(name = "pageSize", defaultValue = "10") int limit)
            throws UnknownHostException, IOException, AnnotationMetadataNotFoundException {
        initializeQuery();

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch37"));

        if (files != null && !files.isEmpty()) {
            queryOptions.put("files", files);
        }
        List<VariantRepositoryFilter> filters = new FilterBuilder().withFiles(files).build();

        PageRequest pageRequest = Utils.getPageRequest(limit, pageToken);

        Region region = new Region(chromosome, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);

        List<VariantWithSamplesAndAnnotation> variantEntities = service.findByRegionsAndComplexFilters(regions, filters,
                                                                                                       null, null,
                                                                                                       pageRequest);
        List<VariantWithSamplesAndAnnotation> variants = Collections.unmodifiableList(variantEntities);

        Long numTotalResults = service.countByRegionsAndComplexFilters(regions, filters);

        // Convert Variant objects to GAVariant
        List<GAVariant> gaVariants = GAVariantFactory.create(variants);
        // Calculate the next page token
        String nextPageToken = Utils.getNextPageToken(pageRequest, limit, numTotalResults);

        // Create the custom response for the GA4GH API
        return new GASearchVariantsResponse(gaVariants, nextPageToken);
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST, consumes = "application/json")
    public GASearchVariantsResponse getVariantsByRegion(GASearchVariantRequest request)
            throws UnknownHostException, IOException, AnnotationMetadataNotFoundException {
        request.validate();
        return getVariantsByRegion(request.getReferenceName(), (int) request.getStart(), (int) request.getEnd(),
                request.getVariantSetIds(), request.getPageToken(), request.getPageSize());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public void handleException(IllegalArgumentException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.PAYLOAD_TOO_LARGE.value(), e.getMessage());
    }

}
