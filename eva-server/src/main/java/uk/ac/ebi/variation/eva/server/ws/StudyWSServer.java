package uk.ac.ebi.variation.eva.server.ws;

import com.mongodb.BasicDBObject;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.lib.auth.MongoCredentials;
import org.opencb.opencga.storage.variant.StudyDBAdaptor;
import org.opencb.opencga.storage.variant.mongodb.DBObjectToVariantSourceConverter;
import org.opencb.opencga.storage.variant.mongodb.StudyMongoDBAdaptor;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Path("/{version}/studies")
@Produces(MediaType.APPLICATION_JSON)
public class StudyWSServer extends EvaWSServer {
    
    private StudyDBAdaptor studyMongoQueryBuilder;
    private MongoCredentials credentials;

    public StudyWSServer() {

    }

    public StudyWSServer(@DefaultValue("") @PathParam("version") String version, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws IOException {
        super(version, uriInfo, hsr);
        try {
            credentials = new MongoCredentials("mongos-hxvm-001", 27017, "eva_hsapiens", "biouser", "biopass");
            studyMongoQueryBuilder = new StudyMongoDBAdaptor(credentials);
        } catch (IllegalOpenCGACredentialsException e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("/list")
    public Response getStudies() {
        return createOkResponse(studyMongoQueryBuilder.listStudies());
    }
    
    @GET
    @Path("/{study}/files")
    public Response getFilesByStudy(@PathParam("study") String study) {
        QueryResult idQueryResult = studyMongoQueryBuilder.findStudyNameOrStudyId(study, queryOptions);
        if (idQueryResult.getNumResults() == 0) {
            QueryResult queryResult = new QueryResult();
            queryResult.setErrorMsg("Study identifier not found");
            return createOkResponse(queryResult);
        }
        
        BasicDBObject id = (BasicDBObject) idQueryResult.getResult().get(0);
        QueryResult finalResult = studyMongoQueryBuilder.getAllSourcesByStudyId(id.getString(DBObjectToVariantSourceConverter.STUDYID_FIELD), queryOptions);
        finalResult.setDbTime(finalResult.getDbTime() + idQueryResult.getDbTime());
        return createOkResponse(finalResult);
    }
    
    @GET
    @Path("/{study}/view")
    public Response getStudy(@PathParam("study") String study) {
        QueryResult idQueryResult = studyMongoQueryBuilder.findStudyNameOrStudyId(study, queryOptions);
        if (idQueryResult.getNumResults() == 0) {
            QueryResult queryResult = new QueryResult();
            queryResult.setErrorMsg("Study identifier not found");
            return createOkResponse(queryResult);
        }
        
        BasicDBObject id = (BasicDBObject) idQueryResult.getResult().get(0);
        QueryResult finalResult = studyMongoQueryBuilder.getStudyById(id.getString(DBObjectToVariantSourceConverter.STUDYID_FIELD), queryOptions);
        finalResult.setDbTime(finalResult.getDbTime() + idQueryResult.getDbTime());
        return createOkResponse(finalResult);
    }
}
