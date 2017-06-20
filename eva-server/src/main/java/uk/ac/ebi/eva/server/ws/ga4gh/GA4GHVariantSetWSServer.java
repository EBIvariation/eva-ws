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
import uk.ac.ebi.eva.commons.core.models.VariantSource;
import uk.ac.ebi.eva.commons.mongodb.services.VariantSourceService;
import uk.ac.ebi.eva.lib.models.ga4gh.GASearchVariantSetsRequest;
import uk.ac.ebi.eva.lib.models.ga4gh.GASearchVariantSetsResponse;
import uk.ac.ebi.eva.lib.models.ga4gh.GAVariantSet;
import uk.ac.ebi.eva.lib.models.ga4gh.GAVariantSetFactory;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.server.Utils;
import uk.ac.ebi.eva.server.ws.EvaWSServer;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/ga4gh/variantsets", produces = "application/json")
@Api(tags = { "ga4gh", "files" })
public class GA4GHVariantSetWSServer extends EvaWSServer {

    @Autowired
    private VariantSourceService service;

    protected static Logger logger = LoggerFactory.getLogger(GA4GHVariantSetWSServer.class);
    
    public GA4GHVariantSetWSServer() { }
    
    /**
     * 
     * @see http://ga4gh.org/documentation/api/v0.5/ga4gh_api.html#/schema/org.ga4gh.GASearchVariantSetsRequest
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public GASearchVariantSetsResponse getVariantSets(@RequestParam(name = "datasetIds") List<String> studies,
                                                      @RequestParam(name = "pageToken", required = false) String pageToken,
                                                      @RequestParam(name = "pageSize", defaultValue = "10") int limit)
            throws IOException {
        initializeQuery();

        if (studies.isEmpty()) {
            throw new IllegalArgumentException("The 'datasetIds' argument must not be empty");
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch37"));

        PageRequest pageRequest = Utils.getPageRequest(limit, pageToken);

        List<VariantSource> variantSources = service.findByStudyIdIn(studies, pageRequest);
        Long numTotalResults = service.countByStudyIdIn(studies);

        // Convert VariantSource objects to GAVariantSet
        List<GAVariantSet> gaVariantSets = GAVariantSetFactory.create(variantSources);
        // Calculate the next page token
        String nextPageToken = Utils.getNextPageToken(pageRequest, limit, numTotalResults);

        // Create the custom response for the GA4GH API
        return new GASearchVariantSetsResponse(gaVariantSets, nextPageToken);
    }
    
    @RequestMapping(value = "/search", method = RequestMethod.POST, consumes = "application/json")
    public GASearchVariantSetsResponse getVariantSets(GASearchVariantSetsRequest request)
            throws IOException {
        return getVariantSets(request.getDatasetIds(), request.getPageToken(), request.getPageSize());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public void handleException(IllegalArgumentException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

}
