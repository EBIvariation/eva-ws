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
import org.opencb.biodata.ga4gh.GASearchVariantSetsRequest;
import org.opencb.biodata.ga4gh.GASearchVariantSetsResponse;
import org.opencb.biodata.ga4gh.GAVariantSet;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.ga4gh.GAVariantSetFactory;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
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

import uk.ac.ebi.eva.lib.repository.VariantSourceEntityRepository;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.server.Utils;
import uk.ac.ebi.eva.server.ws.EvaWSServer;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/ga4gh/variantsets", produces = "application/json")
@Api(tags = { "ga4gh", "files" })
public class GA4GHVariantSetWSServer extends EvaWSServer {

    @Autowired
    private VariantSourceEntityRepository repository;

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
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        initializeQuery();

        if (studies.isEmpty()) {
            throw new IllegalArgumentException("The 'datasetIds' argument must not be empty");
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch37"));

        PageRequest pageRequest = Utils.getPageRequest(limit, pageToken);

        List<VariantSource> variantSources =
                Utils.convertVariantSourceEntitiesToVariantSources(repository.findByStudyIdIn(studies, pageRequest));
        Long numTotalResults = repository.countByStudyIdIn(studies);

        // Convert VariantSource objects to GAVariantSet
        List<GAVariantSet> gaVariantSets = GAVariantSetFactory.create(variantSources);
        // Calculate the next page token
        String nextPageToken = Utils.getNextPageToken(pageRequest, limit, numTotalResults);

        // Create the custom response for the GA4GH API
        return new GASearchVariantSetsResponse(gaVariantSets, nextPageToken);
    }
    
    @RequestMapping(value = "/search", method = RequestMethod.POST, consumes = "application/json")
    public GASearchVariantSetsResponse getVariantSets(GASearchVariantSetsRequest request)
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        return getVariantSets(request.getDatasetIds(), request.getPageToken(), request.getPageSize());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public void handleException(IllegalArgumentException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

}
