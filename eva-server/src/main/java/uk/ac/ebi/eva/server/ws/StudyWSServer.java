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

import java.io.IOException;
import java.net.UnknownHostException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;

import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.adaptors.StudyDBAdaptor;
import org.opencb.opencga.storage.core.variant.adaptors.VariantSourceDBAdaptor;
import org.opencb.opencga.storage.mongodb.variant.DBObjectToVariantSourceConverter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;

import io.swagger.annotations.Api;
import uk.ac.ebi.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.storage.metadata.StudyDgvaDBAdaptor;
import uk.ac.ebi.eva.lib.storage.metadata.StudyEvaproDBAdaptor;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@RestController
@RequestMapping(value = "/v1/studies", produces = "application/json")
@Api(tags = { "studies" })
public class StudyWSServer extends EvaWSServer {

    private StudyDBAdaptor studyDgvaDbAdaptor;
    private StudyDBAdaptor studyEvaproDbAdaptor;

    public StudyWSServer() throws NamingException, IOException {
        studyDgvaDbAdaptor = new StudyDgvaDBAdaptor();
        studyEvaproDbAdaptor = new StudyEvaproDBAdaptor();
    }

    @RequestMapping(value = "/{study}/files", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "Retrieves all the files from a study", response = QueryResponse.class)
    public QueryResponse getFilesByStudy(@PathVariable("study") String study,
                                         @RequestParam("species") String species,
                                         HttpServletResponse response)
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        initializeQueryOptions();
            
        StudyDBAdaptor studyMongoDbAdaptor = DBAdaptorConnector.getStudyDBAdaptor(species);
        VariantSourceDBAdaptor variantSourceDbAdaptor = DBAdaptorConnector.getVariantSourceDBAdaptor(species);
        
        QueryResult idQueryResult = studyMongoDbAdaptor.findStudyNameOrStudyId(study, queryOptions);
        if (idQueryResult.getNumResults() == 0) {
            QueryResult queryResult = new QueryResult();
            queryResult.setErrorMsg("Study identifier not found");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(queryResult);
        }

        BasicDBObject id = (BasicDBObject) idQueryResult.getResult().get(0);
        QueryResult finalResult = variantSourceDbAdaptor.getAllSourcesByStudyId(
                id.getString(DBObjectToVariantSourceConverter.STUDYID_FIELD), queryOptions);
        finalResult.setDbTime(finalResult.getDbTime() + idQueryResult.getDbTime());

        return setQueryResponse(finalResult);
    }

    @RequestMapping(value = "/{study}/view", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "The info of a study", response = QueryResponse.class)
    public QueryResponse getStudy(@PathVariable("study") String study,
                                  @RequestParam(name = "species") String species,
                                  HttpServletResponse response)
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        initializeQueryOptions();
        
        StudyDBAdaptor studyMongoDbAdaptor = DBAdaptorConnector.getStudyDBAdaptor(species);
        
        QueryResult idQueryResult = studyMongoDbAdaptor.findStudyNameOrStudyId(study, queryOptions);
        if (idQueryResult.getNumResults() == 0) {
            QueryResult queryResult = new QueryResult();
            queryResult.setErrorMsg("Study identifier not found");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(queryResult);
        }

        BasicDBObject id = (BasicDBObject) idQueryResult.getResult().get(0);
        QueryResult finalResult = studyMongoDbAdaptor.getStudyById(
            id.getString(DBObjectToVariantSourceConverter.STUDYID_FIELD), queryOptions);
        finalResult.setDbTime(finalResult.getDbTime() + idQueryResult.getDbTime());

        return setQueryResponse(finalResult);
    }

    @RequestMapping(value = "/{study}/summary", method = RequestMethod.GET)
    public QueryResponse getStudySummary(@PathVariable("study") String study,
                                         @RequestParam(name = "structural", defaultValue = "false") boolean structural) {
        if (structural) {
            return setQueryResponse(studyDgvaDbAdaptor.getStudyById(study, queryOptions));
        } else {
            return setQueryResponse(studyEvaproDbAdaptor.getStudyById(study, queryOptions));
        }
    }
}
