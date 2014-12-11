package uk.ac.ebi.variation.eva.server.ws;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.opencb.opencga.storage.variant.VariantSourceDBAdaptor;
import uk.ac.ebi.variation.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.variation.eva.lib.storage.metadata.VariantSourceEvaproDBAdaptor;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Path("/{version}/files")
@Produces("application/json")
@Api(value = "Files", description = "Files RESTful Web Services API")
public class FilesWSServer extends EvaWSServer {

    private VariantSourceDBAdaptor variantSourceEvaproDbAdaptor;


    public FilesWSServer(@DefaultValue("") @PathParam("version") String version,
                         @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws NamingException {
        super(version, uriInfo, hsr);
        variantSourceEvaproDbAdaptor = new VariantSourceEvaproDBAdaptor();
    }

    @GET
    @Path("/all")
    @ApiOperation(httpMethod = "GET", value = "Gets the files of a species")
    public Response getFiles(@QueryParam("species") String species) {
        try {
            checkParams();
//            if (species != null && !species.isEmpty()) {
//                queryOptions.put("species", species);
//            }
            VariantSourceDBAdaptor variantSourceMongoDbAdaptor = DBAdaptorConnector.getVariantSourceDBAdaptor(species);
            return createOkResponse(variantSourceMongoDbAdaptor.getAllSources(queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e.toString());
        }
    }

    @GET
    @Path("/{files}/url")
    @ApiOperation(httpMethod = "GET", value = "Gets the URL of a file")
    public Response getFileUrl(@PathParam("files") String filenames) {
        try {
            checkParams();
            return createOkResponse(variantSourceEvaproDbAdaptor.getSourceDownloadUrlByName(Arrays.asList(filenames.split(","))));
        } catch (Exception e) {
            return createErrorResponse(e.toString());
        }
    }

}
