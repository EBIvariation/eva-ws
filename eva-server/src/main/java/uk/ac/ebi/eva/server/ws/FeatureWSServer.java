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
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.eva.commons.models.data.FeatureCoordinates;
import uk.ac.ebi.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.datastore.FeatureRepository;
import uk.ac.ebi.eva.lib.datastore.MultiMongoDbFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Jose Miguel Mut Lopez &lt;jmmut@ebi.ac.uk&gt;
 */

@RestController
@RequestMapping(value = "/v1/features", produces = "application/json")
@Api(tags = { "features" })
@EnableMongoRepositories("uk.ac.ebi.eva.lib.datastore")
public class FeatureWSServer extends EvaWSServer {

    @Autowired
    private FeatureRepository featureRepository;

    protected static Logger logger = LoggerFactory.getLogger(FeatureWSServer.class);

    @RequestMapping(value = "/{featureIdOrName}", method = RequestMethod.GET)
    public QueryResponse getFeatureByIdOrName(@PathVariable("featureIdOrName") String featureIdOrName,
                                              @RequestParam("species") String species,
                                              HttpServletResponse response)
            throws IllegalOpenCGACredentialsException, IOException {

        initializeQueryOptions();

        if (species == null || species.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse("Please specify a species");
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));

        List<FeatureCoordinates> features = featureRepository.findByIdOrName(featureIdOrName, featureIdOrName);

        QueryResult<FeatureCoordinates> queryResult = new QueryResult<>();
        queryResult.setResult(features);
        return setQueryResponse(queryResult);
    }

}
