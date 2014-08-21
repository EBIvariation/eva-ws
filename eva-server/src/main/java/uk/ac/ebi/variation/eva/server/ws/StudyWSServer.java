package uk.ac.ebi.variation.eva.server.ws;

import com.mongodb.BasicDBObject;
import java.io.IOException;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.variant.StudyDBAdaptor;
import org.opencb.opencga.storage.variant.VariantSourceDBAdaptor;
import org.opencb.opencga.storage.variant.mongodb.DBObjectToVariantSourceConverter;
import org.opencb.opencga.storage.variant.mongodb.StudyMongoDBAdaptor;
import org.opencb.opencga.storage.variant.mongodb.VariantSourceMongoDBAdaptor;
import uk.ac.ebi.variation.eva.lib.storage.metadata.StudyDgvaDBAdaptor;
import uk.ac.ebi.variation.eva.lib.storage.metadata.StudyEvaproDBAdaptor;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Path("/{version}/studies")
@Produces(MediaType.APPLICATION_JSON)
public class StudyWSServer extends EvaWSServer {
    
    private StudyDBAdaptor studyDgvaDbAdaptor;
    private StudyDBAdaptor studyEvaproDbAdaptor;
    private StudyDBAdaptor studyMongoDbAdaptor;
    private VariantSourceDBAdaptor variantSourceDbAdaptor;

    public StudyWSServer() throws IllegalOpenCGACredentialsException {
        super();
    }

    public StudyWSServer(@DefaultValue("") @PathParam("version") String version, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) 
            throws IOException, IllegalOpenCGACredentialsException, NamingException {
        super(version, uriInfo, hsr);
        studyDgvaDbAdaptor = new StudyDgvaDBAdaptor();
        studyEvaproDbAdaptor = new StudyEvaproDBAdaptor();
        studyMongoDbAdaptor = new StudyMongoDBAdaptor(credentials);
        variantSourceDbAdaptor = new VariantSourceMongoDBAdaptor(credentials);
    }

    @GET
    @Path("/{study}/files")
    public Response getFilesByStudy(@PathParam("study") String study) {
        QueryResult idQueryResult = studyMongoDbAdaptor.findStudyNameOrStudyId(study, queryOptions);
        if (idQueryResult.getNumResults() == 0) {
            QueryResult queryResult = new QueryResult();
            queryResult.setErrorMsg("Study identifier not found");
            return createOkResponse(queryResult);
        }
        
        BasicDBObject id = (BasicDBObject) idQueryResult.getResult().get(0);
        QueryResult finalResult = variantSourceDbAdaptor.getAllSourcesByStudyId(id.getString(DBObjectToVariantSourceConverter.STUDYID_FIELD), queryOptions);
        finalResult.setDbTime(finalResult.getDbTime() + idQueryResult.getDbTime());
        return createOkResponse(finalResult);
    }
    
    @GET
    @Path("/{study}/view")
    public Response getStudy(@PathParam("study") String study) {
        QueryResult idQueryResult = studyMongoDbAdaptor.findStudyNameOrStudyId(study, queryOptions);
        if (idQueryResult.getNumResults() == 0) {
            QueryResult queryResult = new QueryResult();
            queryResult.setErrorMsg("Study identifier not found");
            return createOkResponse(queryResult);
        }
        
        BasicDBObject id = (BasicDBObject) idQueryResult.getResult().get(0);
        QueryResult finalResult = studyMongoDbAdaptor.getStudyById(id.getString(DBObjectToVariantSourceConverter.STUDYID_FIELD), queryOptions);
        finalResult.setDbTime(finalResult.getDbTime() + idQueryResult.getDbTime());
        return createOkResponse(finalResult);
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
