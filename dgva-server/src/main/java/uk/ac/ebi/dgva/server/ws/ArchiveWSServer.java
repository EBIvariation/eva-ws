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

package uk.ac.ebi.dgva.server.ws;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.lib.metadata.dgva.ArchiveDgvaDBAdaptor;
import uk.ac.ebi.eva.lib.metadata.dgva.StudyDgvaDBAdaptor;
import uk.ac.ebi.eva.lib.utils.QueryOptions;
import uk.ac.ebi.eva.lib.utils.QueryResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping(value = "/v1/meta", produces = "application/json")
@Api(tags = {"archive"})
public class ArchiveWSServer extends DgvaWSServer {

    @Autowired
    private ArchiveDgvaDBAdaptor archiveDgvaDbAdaptor;

    @Autowired
    private StudyDgvaDBAdaptor studyDgvaDbAdaptor;

    private Properties properties;
    
    public ArchiveWSServer() throws IOException {
    }

    @RequestMapping(value = "/studies/all", method = RequestMethod.GET)
    public QueryResponse getStudies(@RequestParam(name = "species", required = false) List<String> species,
                                    @RequestParam(name = "type", required = false) List<String> types) {
        initializeQuery();
        QueryOptions queryOptions = getQueryOptions();
        if (species != null && !species.isEmpty()) {
            queryOptions.put("species", species);
        }
        if (types != null && !types.isEmpty()) {
            queryOptions.put("type", types);
        }

        return setQueryResponse(studyDgvaDbAdaptor.getAllStudies(queryOptions));
    }

    @RequestMapping(value = "/studies/stats", method = RequestMethod.GET)
    public QueryResponse getStudiesStats(@RequestParam(name = "species", required = false) List<String> species,
                                         @RequestParam(name = "type", required = false) List<String> types) {
        initializeQuery();
        if (species != null && !species.isEmpty()) {
            getQueryOptions().put("species", species);
        }
        if (types != null && !types.isEmpty()) {
            getQueryOptions().put("type", types);
        }

        QueryResult<Map.Entry<String, Long>> resultSpecies, resultTypes;

        resultSpecies = archiveDgvaDbAdaptor.countStudiesPerSpecies(getQueryOptions());
        resultTypes = archiveDgvaDbAdaptor.countStudiesPerType(getQueryOptions());

        QueryResult combinedQueryResult = new QueryResult();
        combinedQueryResult.setDbTime(resultSpecies.getDbTime() + resultTypes.getDbTime());

        JsonNodeFactory factory = new JsonNodeFactory(true);
        ObjectNode root = factory.objectNode();
        combinedQueryResult.addResult(root);
        combinedQueryResult.setNumTotalResults(combinedQueryResult.getNumResults());

        // Species
        ObjectNode speciesNode = factory.objectNode();
        for (Map.Entry<String, Long> speciesCount : resultSpecies.getResult()) {
            speciesNode.put(speciesCount.getKey(), speciesCount.getValue());
        }
        root.put("species", speciesNode);

        // Types
        ObjectNode typesNode = factory.objectNode();
        for (Map.Entry<String, Long> typesCount : resultTypes.getResult()) {
            typesNode.put(typesCount.getKey(), typesCount.getValue());
        }
        root.put("type", typesNode);

        return setQueryResponse(combinedQueryResult);
    }
}
