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

package uk.ac.ebi.eva.server.ws.ga4gh;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.ga4gh.GASearchVariantSetsRequest;
import org.opencb.biodata.ga4gh.GASearchVariantSetsResponse;
import org.opencb.biodata.ga4gh.GAVariantSet;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.ga4gh.GAVariantSetFactory;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantSourceDBAdaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;
import uk.ac.ebi.eva.server.ws.EvaWSServer;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@RestController
@RequestMapping(value = "/v1/ga4gh/variantsets", produces = "application/json")
@Api(tags = { "ga4gh", "files" })
public class GA4GHVariantSetWSServer extends EvaWSServer {
    
    public GA4GHVariantSetWSServer() { }
    
    /**
     * 
     * @see http://ga4gh.org/documentation/api/v0.5/ga4gh_api.html#/schema/org.ga4gh.GASearchVariantSetsRequest
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public GASearchVariantSetsResponse getVariantSets(@RequestParam("datasetIds") String studies,
                                                      @RequestParam(name = "pageToken", required = false) String pageToken,
                                                      @RequestParam(name = "pageSize", defaultValue = "10") int limit,
                                                      @RequestParam(name = "histogram", defaultValue = "false") boolean histogram,
                                                      @RequestParam(name = "histogram_interval", defaultValue = "-1") int interval)
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        initializeQueryOptions();
        
        if (studies.isEmpty()) {
            throw new IllegalArgumentException("The 'datasetIds' argument must not be empty");
        }
        
        VariantSourceDBAdaptor dbAdaptor = dbAdaptorConnector.getVariantSourceDBAdaptor("hsapiens_grch37");
        
        int idxCurrentPage = 0;
        if (pageToken != null && !pageToken.isEmpty() && StringUtils.isNumeric(pageToken)) {
            idxCurrentPage = Integer.parseInt(pageToken);
            queryOptions.put("skip", idxCurrentPage * limit);
        }
        queryOptions.put("limit", limit);
        
        List<String> studiesList = Arrays.asList(studies.split(","));
        QueryResult<VariantSource> qr = dbAdaptor.getAllSourcesByStudyIds(studiesList, queryOptions);
        
        // Convert VariantSource objects to GAVariantSet
        List<GAVariantSet> gaVariantSets = GAVariantSetFactory.create(qr.getResult());
        // Calculate the next page token
        int idxLastElement = idxCurrentPage * limit + limit;
        String nextPageToken = (idxLastElement < qr.getNumTotalResults()) ? String.valueOf(idxCurrentPage + 1) : null;

        // Create the custom response for the GA4GH API
        return new GASearchVariantSetsResponse(gaVariantSets, nextPageToken);
    }
    
    @RequestMapping(value = "/search", method = RequestMethod.POST, consumes = "application/json")
    public GASearchVariantSetsResponse getVariantSets(GASearchVariantSetsRequest request)
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        return getVariantSets(StringUtils.join(request.getDatasetIds(), ','), 
                request.getPageToken(), request.getPageSize(), false, -1);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public void handleException(IllegalArgumentException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

}
