/*
 * Copyright 2019 EMBL - European Bioinformatics Institute
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.eva.commons.core.models.DefaultLocusRangeMetadata;
import uk.ac.ebi.eva.commons.mongodb.services.DefaultLocusRangeMetadataService;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/default-locus-range", produces = "application/json")
@Api(tags = {"default-locus-range"})
public class DefaultLocusRangeMetadataWSServer extends EvaWSServer {

    @Autowired
    private DefaultLocusRangeMetadataService service;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public QueryResponse getDefaultLocusRangeMetadata(@RequestParam(name = "species") String species,
                                               HttpServletResponse response) {
        if (species.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse("Please specify a species");
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));
        List<DefaultLocusRangeMetadata> defaultLocusRangeMetadataList =
                service.findAllByOrderByChromosomeAscStartAscEndAsc();
        QueryResult<DefaultLocusRangeMetadata> queryResult = buildQueryResult(defaultLocusRangeMetadataList);
        return setQueryResponse(queryResult);
    }

}
