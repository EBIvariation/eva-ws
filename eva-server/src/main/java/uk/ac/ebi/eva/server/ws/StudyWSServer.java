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

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.commons.mongodb.entities.projections.VariantStudySummary;
import uk.ac.ebi.eva.commons.mongodb.services.VariantSourceService;
import uk.ac.ebi.eva.commons.mongodb.services.VariantStudySummaryService;
import uk.ac.ebi.eva.lib.metadata.dgva.StudyDgvaDBAdaptor;
import uk.ac.ebi.eva.lib.metadata.eva.RoCrateMetadataAdaptor;
import uk.ac.ebi.eva.lib.metadata.eva.StudyEvaproDBAdaptor;
import uk.ac.ebi.eva.lib.models.rocrate.RoCrateMetadata;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/studies", produces = "application/json")
@Api(tags = {"studies"})
public class StudyWSServer extends EvaWSServer {

    @Autowired
    private StudyDgvaDBAdaptor studyDgvaDbAdaptor;
    @Autowired
    private StudyEvaproDBAdaptor studyEvaproDbAdaptor;
    @Autowired
    private RoCrateMetadataAdaptor roCrateMetadataAdaptor;
    @Autowired
    private VariantStudySummaryService variantStudySummaryService;
    @Autowired
    private VariantSourceService variantSourceService;

    @RequestMapping(value = "/{study}/files", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "Retrieves all the files from a study", response = QueryResponse.class)
    public QueryResponse getFilesByStudy(@PathVariable("study") String study,
                                         @RequestParam("species") String species,
                                         HttpServletResponse response)
            throws IOException {
        initializeQuery();

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));
        List variantSourceEntityList = variantSourceService.findByStudyIdOrStudyName(study, study);
        QueryResult queryResult;

        if (variantSourceEntityList.size() == 0) {
            queryResult = buildQueryResult(Collections.emptyList());
            queryResult.setErrorMsg("Study identifier not found");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(queryResult);
        }

        queryResult = buildQueryResult(variantSourceEntityList);
        return setQueryResponse(queryResult);
    }

    @RequestMapping(value = "/{study}/view", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "The info of a study", response = QueryResponse.class)
    public QueryResponse getStudy(@PathVariable("study") String study,
                                  @RequestParam(name = "species") String species,
                                  HttpServletResponse response)
            throws IOException {
        initializeQuery();

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));
        VariantStudySummary variantStudySummary = variantStudySummaryService.findByStudyNameOrStudyId(study);

        QueryResult<VariantStudySummary> queryResult;
        if (variantStudySummary == null) {
            queryResult = buildQueryResult(Collections.emptyList());
            queryResult.setErrorMsg("Study identifier not found");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            queryResult = buildQueryResult(Collections.singletonList(variantStudySummary));
        }
        return setQueryResponse(queryResult);
    }

    @RequestMapping(value = "/{study}/summary", method = RequestMethod.GET)
    public QueryResponse getStudySummary(@PathVariable("study") String study,
                                         @RequestParam(name = "structural", defaultValue = "false") boolean structural) {
        if (structural) {
            return setQueryResponse(studyDgvaDbAdaptor.getStudyById(study, getQueryOptions()));
        } else {
            return setQueryResponse(studyEvaproDbAdaptor.getStudyById(study, getQueryOptions()));
        }
    }

    @RequestMapping(value = "/{study}/ro-crate", method = RequestMethod.GET)
    public RoCrateMetadata getStudyRoCrate(@PathVariable("study") String study) {
        RoCrateMetadata roCrateMetadata = roCrateMetadataAdaptor.getMetadataByProjectAccession(study);
        return roCrateMetadata;
    }
}
