package uk.ac.ebi.variation.eva.server.ws;

import java.io.IOException;
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
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.lib.auth.MongoCredentials;
import org.opencb.opencga.storage.variant.VariantDBAdaptor;
import org.opencb.opencga.storage.variant.mongodb.VariantMongoDBAdaptor;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Path("/{version}/genes/")
@Produces(MediaType.APPLICATION_JSON)
public class GeneWSServer extends EvaWSServer {

    private VariantDBAdaptor variantMongoQueryBuilder;
    private MongoCredentials credentials;

    public GeneWSServer() {

    }

    public GeneWSServer(@DefaultValue("") @PathParam("version")String version, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws IOException {
        super(version, uriInfo, hsr);
        try {
            credentials = new MongoCredentials("mongos-hxvm-dev-001", 27017, "eva_hsapiens", "biouser", "biopass");
            variantMongoQueryBuilder = new VariantMongoDBAdaptor(credentials);
        } catch (IllegalOpenCGACredentialsException e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("{gene}/variants")
    public Response getVariantsByGene(@PathParam("gene") String geneId) {
        return createOkResponse(variantMongoQueryBuilder.getAllVariantsByGene(geneId, queryOptions));
    }
    
    @GET
    @Path("/ranking")
    public Response getVariantsByGene(@PathParam("gene") String geneId, 
                                      @DefaultValue("10") @QueryParam("limit") int limit,
                                      @DefaultValue("desc") @QueryParam("sort") String sort) {
        if (sort.equalsIgnoreCase("desc")) {
            return createOkResponse(variantMongoQueryBuilder.getMostAffectedGenes(limit, queryOptions));
        } else if (sort.equalsIgnoreCase("asc")) {
            return createOkResponse(variantMongoQueryBuilder.getLeastAffectedGenes(limit, queryOptions));
        } else {
            return createOkResponse("Sorting criteria must be 'desc' or 'asc'");
        }
    }
    
    
}
