/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014, 2015 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.variation.eva.server.ws;

import java.io.IOException;
import java.net.UnknownHostException;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import com.mongodb.BasicDBObject;
import io.swagger.annotations.Api;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.adaptors.StudyDBAdaptor;
import org.opencb.opencga.storage.core.variant.adaptors.VariantSourceDBAdaptor;
import org.opencb.opencga.storage.mongodb.variant.DBObjectToVariantSourceConverter;
import uk.ac.ebi.variation.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.variation.eva.lib.storage.metadata.StudyDgvaDBAdaptor;
import uk.ac.ebi.variation.eva.lib.storage.metadata.StudyEvaproDBAdaptor;
import uk.ac.ebi.variation.eva.server.exception.SpeciesException;
import uk.ac.ebi.variation.eva.server.exception.VersionException;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Path("/v1/studies")
@Produces("application/json")
@Api(tags = { "studies" })
public class StudyWSServer extends EvaWSServer {

    private StudyDBAdaptor studyDgvaDbAdaptor;
    private StudyDBAdaptor studyEvaproDbAdaptor;


    public StudyWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws IOException, NamingException {
        super(uriInfo, hsr);
        studyDgvaDbAdaptor = new StudyDgvaDBAdaptor();
        studyEvaproDbAdaptor = new StudyEvaproDBAdaptor();
    }

    @GET
    @Path("/{study}/files")
//    @ApiOperation(httpMethod = "GET", value = "Retrieves all the files from a study", response = QueryResponse.class)
    public Response getFilesByStudy(@PathParam("study") String study,
                                    @QueryParam("species") String species) 
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        try {
            checkParams();
        } catch (VersionException | SpeciesException ex) {
            return createErrorResponse(ex.toString());
        }
            
        StudyDBAdaptor studyMongoDbAdaptor = DBAdaptorConnector.getStudyDBAdaptor(species);
        VariantSourceDBAdaptor variantSourceDbAdaptor = DBAdaptorConnector.getVariantSourceDBAdaptor(species);
        
        QueryResult idQueryResult = studyMongoDbAdaptor.findStudyNameOrStudyId(study, queryOptions);
        if (idQueryResult.getNumResults() == 0) {
            QueryResult queryResult = new QueryResult();
            queryResult.setErrorMsg("Study identifier not found");
            return createOkResponse(queryResult);
        }

        BasicDBObject id = (BasicDBObject) idQueryResult.getResult().get(0);
        QueryResult finalResult = variantSourceDbAdaptor.getAllSourcesByStudyId(
                id.getString(DBObjectToVariantSourceConverter.STUDYID_FIELD), queryOptions);
        finalResult.setDbTime(finalResult.getDbTime() + idQueryResult.getDbTime());

        return createOkResponse(translateVariantSourceFileIds(finalResult));
    }

    @GET
    @Path("/{study}/view")
//    @ApiOperation(httpMethod = "GET", value = "The info of a study", response = QueryResponse.class)
    public Response getStudy(@PathParam("study") String study,
                             @QueryParam("species") String species) 
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        try {
            checkParams();
        } catch (VersionException | SpeciesException ex) {
            return createErrorResponse(ex.toString());
        }
        
        StudyDBAdaptor studyMongoDbAdaptor = DBAdaptorConnector.getStudyDBAdaptor(species);
        
        QueryResult idQueryResult = studyMongoDbAdaptor.findStudyNameOrStudyId(study, queryOptions);
        if (idQueryResult.getNumResults() == 0) {
            QueryResult queryResult = new QueryResult();
            queryResult.setErrorMsg("Study identifier not found");
            return createOkResponse(queryResult);
        }

        BasicDBObject id = (BasicDBObject) idQueryResult.getResult().get(0);
        QueryResult finalResult = studyMongoDbAdaptor.getStudyById(
            id.getString(DBObjectToVariantSourceConverter.STUDYID_FIELD), queryOptions);
        finalResult.setDbTime(finalResult.getDbTime() + idQueryResult.getDbTime());

        return createOkResponse(translateVariantSourceFileIds(finalResult));
    }

    @GET
    @Path("/{study}/summary")
    public Response getStudySummary(@PathParam("study") String study,
                                    @DefaultValue("false") @QueryParam("structural") boolean structural) {
        if (structural) {
            return createOkResponse(studyDgvaDbAdaptor.getStudyById(study, queryOptions));
        } else {
            return createOkResponse(studyEvaproDbAdaptor.getStudyById(study, queryOptions));
        }
    }
}
