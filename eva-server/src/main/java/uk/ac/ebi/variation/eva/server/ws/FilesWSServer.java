package uk.ac.ebi.variation.eva.server.ws;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import java.util.Properties;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantSourceDBAdaptor;
import uk.ac.ebi.variation.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.variation.eva.lib.storage.metadata.VariantSourceEvaproDBAdaptor;
import uk.ac.ebi.variation.eva.server.exception.SpeciesException;
import uk.ac.ebi.variation.eva.server.exception.VersionException;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Path("/{version}/files")
@Produces("application/json")
@Api(value = "Files", description = "Files RESTful Web Services API")
public class FilesWSServer extends EvaWSServer {

    private final VariantSourceDBAdaptor variantSourceEvaproDbAdaptor;


    public FilesWSServer(@DefaultValue("") @PathParam("version") String version,
                         @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws NamingException, IOException {
        super(version, uriInfo, hsr);
        Properties properties = new Properties(); 
        properties.load(DBAdaptorConnector.class.getResourceAsStream("/mongo.properties"));
        variantSourceEvaproDbAdaptor = new VariantSourceEvaproDBAdaptor(properties.getProperty("eva.version"));
    }

    @GET
    @Path("/all")
    @ApiOperation(httpMethod = "GET", value = "Gets the files of a species")
    public Response getFiles(@QueryParam("species") String species) 
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        try {
            checkParams();
        } catch (VersionException | SpeciesException ex) {
            return createErrorResponse(ex.toString());
        }
        
        VariantSourceDBAdaptor variantSourceMongoDbAdaptor = DBAdaptorConnector.getVariantSourceDBAdaptor(species);
        return createOkResponse(variantSourceMongoDbAdaptor.getAllSources(queryOptions));
    }

    @GET
    @Path("/{files}/url")
    @ApiOperation(httpMethod = "GET", value = "Gets the URL of a file")
    public Response getFileUrl(@PathParam("files") String filenames) {
        try {
            checkParams();
        } catch (VersionException | SpeciesException ex) {
            return createErrorResponse(ex.toString());
        }
        
        return createOkResponse(variantSourceEvaproDbAdaptor.getSourceDownloadUrlByName(Arrays.asList(filenames.split(","))));
    }

}
