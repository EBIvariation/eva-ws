package uk.ac.ebi.variation.eva.server.ws.ga4gh;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.opencb.biodata.ga4gh.GASearchCallSetsRequest;
import org.opencb.biodata.ga4gh.GASearchCallSetsResponse;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.variant.VariantDBAdaptor;
import org.opencb.opencga.storage.variant.mongodb.VariantMongoDBAdaptor;
import uk.ac.ebi.variation.eva.server.ws.EvaWSServer;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Path("/{version}/ga4gh/callsets")
@Produces(MediaType.APPLICATION_JSON)
public class GA4GHVariantCallSetWSServer extends EvaWSServer {
    
    private VariantDBAdaptor variantMongoDbAdaptor;

    public GA4GHVariantCallSetWSServer() throws IllegalOpenCGACredentialsException {
        super();
    }

    public GA4GHVariantCallSetWSServer(@DefaultValue("") @PathParam("version")String version, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) 
            throws IOException, IllegalOpenCGACredentialsException {
        super(version, uriInfo, hsr);
        variantMongoDbAdaptor = new VariantMongoDBAdaptor(credentials);
    }

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getCallSets(GASearchCallSetsRequest request) {
        return createJsonResponse(new GASearchCallSetsResponse(null, null));
    }
    
}
