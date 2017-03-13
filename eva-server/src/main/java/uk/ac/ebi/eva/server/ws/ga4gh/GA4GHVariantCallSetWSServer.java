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
import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.ga4gh.GACallSet;
import org.opencb.biodata.ga4gh.GASearchCallSetsRequest;
import org.opencb.biodata.ga4gh.GASearchCallSetsResponse;
import org.opencb.biodata.models.variant.ga4gh.GACallSetFactory;
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

import uk.ac.ebi.eva.commons.models.data.VariantSourceEntity;
import uk.ac.ebi.eva.lib.repository.VariantSourceEntityRepository;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.server.Utils;
import uk.ac.ebi.eva.server.ws.EvaWSServer;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/v1/ga4gh/callsets", produces = "application/json")
@Api(tags = { "ga4gh", "samples" })
public class GA4GHVariantCallSetWSServer extends EvaWSServer {

    @Autowired
    private VariantSourceEntityRepository repository;

    protected static Logger logger = LoggerFactory.getLogger(GA4GHVariantCallSetWSServer.class);
    
    public GA4GHVariantCallSetWSServer() { }
    
    /**
     * 
     * @see http://ga4gh.org/documentation/api/v0.5/ga4gh_api.html#/schema/org.ga4gh.GASearchCallSetsRequest
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public GASearchCallSetsResponse getCallSets(@RequestParam("variantSetIds") List<String> files,
                                                @RequestParam(name = "pageToken", required = false) String pageToken,
                                                @RequestParam(name = "pageSize", defaultValue = "10") int limit,
                                                @RequestParam(name = "histogram", defaultValue = "false") boolean histogram,
                                                @RequestParam(name = "histogram_interval", defaultValue = "-1") int interval)
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        initializeQuery();
        
        if (files.isEmpty()) {
            throw new IllegalArgumentException("The 'variantSetIds' argument must not be empty");
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch37"));

        int idxCurrentPage = 0;
        if (pageToken != null && !pageToken.isEmpty() && StringUtils.isNumeric(pageToken)) {
            idxCurrentPage = Integer.parseInt(pageToken);
            queryOptions.put("skip", idxCurrentPage * limit);
        }
        queryOptions.put("limit", limit);

        PageRequest pageRequest = Utils.getPageRequest(queryOptions);

        List<VariantSourceEntity> variantSourceEntities = repository.findByFileIdIn(files, pageRequest);
        long numTotalResults = repository.countByFileIdIn(files);

        List<String> fileIds = variantSourceEntities.stream()
                                                    .map(VariantSourceEntity::getFileId)
                                                    .collect(Collectors.toList());

        List<List<String>> samplesLists  = variantSourceEntities.stream()
                                                                .map(VariantSourceEntity::getSamplesPosition)
                                                                .map(Map::keySet)
                                                                .map(ArrayList::new)
                                                                .collect(Collectors.toList());

        // Convert sample names objects to GACallSet
        List<GACallSet> gaCallSets = GACallSetFactory.create(fileIds, samplesLists);
        // Calculate the next page token
        int idxLastElement = idxCurrentPage * limit + limit;
        String nextPageToken = (idxLastElement < numTotalResults) ? String.valueOf(idxCurrentPage + 1) : null;

        // Create the custom response for the GA4GH API
        return new GASearchCallSetsResponse(gaCallSets, nextPageToken);
    }
    
    @RequestMapping(value = "/search", method = RequestMethod.POST, consumes = "application/json")
    public GASearchCallSetsResponse getCallSets(GASearchCallSetsRequest request)
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        return getCallSets(request.getVariantSetIds(), request.getPageToken(), request.getPageSize(), false, -1);
    }
  
    @ExceptionHandler(IllegalArgumentException.class)
    public void handleException(IllegalArgumentException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

}
