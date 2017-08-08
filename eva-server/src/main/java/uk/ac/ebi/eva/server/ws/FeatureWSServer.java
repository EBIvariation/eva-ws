/*
 * Copyright 2016 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import uk.ac.ebi.eva.commons.core.models.FeatureCoordinates;
import uk.ac.ebi.eva.commons.mongodb.services.FeatureService;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.lib.utils.QueryUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/features", produces = "application/json")
@Api(tags = { "features" })
public class FeatureWSServer extends EvaWSServer {

    @Autowired
    private FeatureService service;

    @Autowired
    private QueryUtils queryUtils;

    protected static Logger logger = LoggerFactory.getLogger(FeatureWSServer.class);

    @RequestMapping(value = "/{featureIdOrName}", method = RequestMethod.GET)
    public QueryResponse getFeatureByIdOrName(@PathVariable("featureIdOrName") String featureIdOrName,
                                              @RequestParam("species") String species,
                                              HttpServletResponse response)
            throws IOException {
        queryUtils.initializeQuery();

        if (species.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return queryUtils.setErrorQueryResponse("Please specify a species", this.version);
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));

        List<FeatureCoordinates> features = service.findByIdOrName(featureIdOrName, featureIdOrName);

        QueryResult<FeatureCoordinates> queryResult = queryUtils.buildQueryResult(features);
        return queryUtils.setQueryResponse(queryResult, this.version);
    }

}
