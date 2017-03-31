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

package uk.ac.ebi.eva.server.ws;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.lib.metadata.ArchiveDgvaDBAdaptor;
import uk.ac.ebi.eva.lib.metadata.ArchiveEvaproDBAdaptor;
import uk.ac.ebi.eva.lib.metadata.StudyDgvaDBAdaptor;
import uk.ac.ebi.eva.lib.metadata.StudyEvaproDBAdaptor;
import uk.ac.ebi.eva.lib.repository.VariantStudySummaryRepository;
import uk.ac.ebi.eva.lib.repository.projections.VariantStudySummary;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.utils.MultiMongoDbFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@RestController
@RequestMapping(value = "/v1/meta", produces = "application/json")
@Api(tags = {"archive"})
public class ArchiveWSServer extends EvaWSServer {

    @Autowired
    private ArchiveDgvaDBAdaptor archiveDgvaDbAdaptor;
    @Autowired
    private ArchiveEvaproDBAdaptor archiveEvaproDbAdaptor;

    @Autowired
    private StudyDgvaDBAdaptor studyDgvaDbAdaptor;
    @Autowired
    private StudyEvaproDBAdaptor studyEvaproDbAdaptor;
    @Autowired
    private VariantStudySummaryRepository variantStudySummaryRepository;

    private Properties properties;
    
    public ArchiveWSServer() throws IOException {
        properties = new Properties();
        properties.load(DBAdaptorConnector.class.getResourceAsStream("/eva.properties"));
    }

    @RequestMapping(value = "/files/count", method = RequestMethod.GET)
    public QueryResponse countFiles() {
        return setQueryResponse(archiveEvaproDbAdaptor.countFiles());
    }

    @RequestMapping(value = "/species/count", method = RequestMethod.GET)
    public QueryResponse countSpecies() {
        return setQueryResponse(archiveEvaproDbAdaptor.countSpecies());
    }

    @RequestMapping(value = "/species/list", method = RequestMethod.GET)
    public QueryResponse getSpecies() {
        return setQueryResponse(archiveEvaproDbAdaptor.getSpecies(properties.getProperty("eva.version"), true));
    }

    @RequestMapping(value = "/studies/count", method = RequestMethod.GET)
    public QueryResponse countStudies() {
        return setQueryResponse(archiveEvaproDbAdaptor.countStudies());
    }

    @RequestMapping(value = "/studies/all", method = RequestMethod.GET)
    public QueryResponse getStudies(@RequestParam(name = "species", required = false) String species,
                                    @RequestParam(name = "type", required = false) String types,
                                    @RequestParam(name = "structural", defaultValue = "false") boolean structural) {
        initializeQuery();
        if (species != null && !species.isEmpty()) {
            queryOptions.put("species", Arrays.asList(species.split(",")));
        }
        if (types != null && !types.isEmpty()) {
            queryOptions.put("type", Arrays.asList(types.split(",")));
        }

        if (structural) {
            return setQueryResponse(studyDgvaDbAdaptor.getAllStudies(queryOptions));
        } else {
            return setQueryResponse(studyEvaproDbAdaptor.getAllStudies(queryOptions));
        }
    }

    @RequestMapping(value = "/studies/list", method = RequestMethod.GET)
    public QueryResponse getBrowsableStudies(@RequestParam("species") String species)
            throws IllegalOpenCGACredentialsException, IOException {
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));
        List<VariantStudySummary> uniqueStudies = variantStudySummaryRepository.findBy();
        QueryResult<VariantStudySummary> result = buildQueryResult(uniqueStudies);
        return setQueryResponse(result);
    }

    @RequestMapping(value = "/studies/stats", method = RequestMethod.GET)
    public QueryResponse getStudiesStats(@RequestParam(name = "species", required = false) List<String> species,
                                         @RequestParam(name = "type", required = false) List<String> types,
                                         @RequestParam(name = "structural", defaultValue = "false") boolean structural) {
        initializeQuery();
        if (species != null && !species.isEmpty()) {
            queryOptions.put("species", species);
        }
        if (types != null && !types.isEmpty()) {
            queryOptions.put("type", types);
        }

        QueryResult<Map.Entry<String, Long>> resultSpecies, resultTypes;

        if (structural) {
            resultSpecies = archiveDgvaDbAdaptor.countStudiesPerSpecies(queryOptions);
            resultTypes = archiveDgvaDbAdaptor.countStudiesPerType(queryOptions);
        } else {
            resultSpecies = archiveEvaproDbAdaptor.countStudiesPerSpecies(queryOptions);
            resultTypes = archiveEvaproDbAdaptor.countStudiesPerType(queryOptions);
        }

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
