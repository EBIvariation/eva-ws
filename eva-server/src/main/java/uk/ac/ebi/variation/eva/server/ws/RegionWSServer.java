package uk.ac.ebi.variation.eva.server.ws;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import org.opencb.biodata.models.feature.Region;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.lib.auth.MongoCredentials;
import org.opencb.opencga.storage.variant.VariantDBAdaptor;
import org.opencb.opencga.storage.variant.mongodb.VariantMongoDBAdaptor;

/**
 * Created by imedina on 01/04/14.
 */
@Path("/{version}/segments/{region}")
@Produces(MediaType.APPLICATION_JSON)
public class RegionWSServer extends EvaWSServer {

    private VariantDBAdaptor variantMongoQueryBuilder;
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
    public Response getVariantsByRegion(@PathParam("region") String regionId,
                                        @DefaultValue("") @QueryParam("type") String variantType,
                                        @QueryParam("ref") String reference,
                                        @QueryParam("alt") String alternate) {
        if (!variantType.isEmpty()) {
            queryOptions.put("type", variantType);
        }
        if (reference != null) {
            queryOptions.put("reference", reference);
        }
        if (alternate != null) {
            queryOptions.put("alternate", alternate);
        }
        
        Region region = Region.parseRegion(regionId);
        return createOkResponse(variantMongoQueryBuilder.getAllVariantsByRegion(region, queryOptions));
    }
}
