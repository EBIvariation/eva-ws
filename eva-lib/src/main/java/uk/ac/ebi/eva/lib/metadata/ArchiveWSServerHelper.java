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

package uk.ac.ebi.eva.lib.metadata;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.adaptors.ArchiveDBAdaptor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ArchiveWSServerHelper {

    public QueryResult getStudiesStats(QueryOptions queryOptions, ArchiveDBAdaptor archiveDBAdaptor) {
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

        return combinedQueryResult;
    }

}
