package uk.ac.ebi.variation.eva.server.ws;

import org.opencb.biodata.models.feature.Region;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.lib.auth.MongoCredentials;
import org.opencb.opencga.storage.variant.mongodb.VariantMongoDBAdaptor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

/**
 * Created by imedina on 01/04/14.
 */
@Path("/{version}/segment/{region}")
@Produces(MediaType.APPLICATION_JSON)
public class RegionWSServer extends EvaWSServer {

    private VariantMongoDBAdaptor variantMongoQueryBuilder;
    private MongoCredentials credentials;

    public RegionWSServer() {

    }

    public RegionWSServer(@DefaultValue("") @PathParam("version")String version, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws IOException {
        super(version, uriInfo, hsr);
        try {
            credentials = new MongoCredentials("mongos-hxvm-dev-001", 27017, "eva_hsapiens", "biouser", "biopass");
            variantMongoQueryBuilder = new VariantMongoDBAdaptor(credentials);
        } catch (IllegalOpenCGACredentialsException e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("/variants")
    public Response getGenesByRegion(@PathParam("region") String chregionId) {
        Region region = Region.parseRegion(chregionId);
        return createOkResponse(variantMongoQueryBuilder.getAllVariantsByRegion(region, queryOptions));
    }
}
