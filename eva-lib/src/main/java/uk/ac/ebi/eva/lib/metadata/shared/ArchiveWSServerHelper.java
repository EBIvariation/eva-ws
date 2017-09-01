/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.lib.metadata.shared;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import uk.ac.ebi.eva.lib.metadata.ArchiveDBAdaptor;
import uk.ac.ebi.eva.lib.metadata.StudyDBAdaptor;
import uk.ac.ebi.eva.lib.utils.QueryOptions;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.lib.utils.QueryUtils;

import java.util.List;
import java.util.Map;

public class ArchiveWSServerHelper {

    public QueryResponse getStudies(List<String> species, List<String> types, QueryUtils queryUtils,
                                    StudyDBAdaptor studyDBAdaptor, String version) {
        queryUtils.initializeQuery();
        if (species != null && !species.isEmpty()) {
            queryUtils.getQueryOptions().put("species", species);
        }
        if (types != null && !types.isEmpty()) {
            queryUtils.getQueryOptions().put("type", types);
        }

        return queryUtils.setQueryResponse(studyDBAdaptor.getAllStudies(queryUtils.getQueryOptions()), version);
    }

    public QueryResponse getStudiesStats(List<String> species, List<String> types, QueryUtils queryUtils,
                                         ArchiveDBAdaptor archiveDBAdaptor, String version) {

        queryUtils.initializeQuery();
        if (species != null && !species.isEmpty()) {
            queryUtils.getQueryOptions().put("species", species);
        }
        if (types != null && !types.isEmpty()) {
            queryUtils.getQueryOptions().put("type", types);
        }

        QueryOptions queryOptions = queryUtils.getQueryOptions();

        QueryResult<Map.Entry<String, Long>> resultSpecies, resultTypes;

        resultSpecies = archiveDBAdaptor.countStudiesPerSpecies(queryOptions);
        resultTypes = archiveDBAdaptor.countStudiesPerType(queryOptions);

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

        return queryUtils.setQueryResponse(combinedQueryResult, version);
    }

}
