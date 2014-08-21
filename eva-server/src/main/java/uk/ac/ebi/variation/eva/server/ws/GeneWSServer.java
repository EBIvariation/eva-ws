package uk.ac.ebi.variation.eva.server.ws;

import java.io.IOException;
import java.util.Arrays;
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
import org.opencb.opencga.storage.variant.VariantDBAdaptor;
import org.opencb.opencga.storage.variant.mongodb.VariantMongoDBAdaptor;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Path("/{version}/genes")
@Produces(MediaType.APPLICATION_JSON)
public class GeneWSServer extends EvaWSServer {

    private VariantDBAdaptor variantMongoQueryBuilder;

    public GeneWSServer() throws IllegalOpenCGACredentialsException {
        super();
    }

    public GeneWSServer(@DefaultValue("") @PathParam("version")String version, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) 
            throws IOException, IllegalOpenCGACredentialsException {
        super(version, uriInfo, hsr);
        variantMongoQueryBuilder = new VariantMongoDBAdaptor(credentials);
    }

    @GET
    @Path("/{gene}/variants")
    public Response getVariantsByGene(@PathParam("gene") String geneId,
                                      @QueryParam("ref") String reference,
                                      @QueryParam("alt") String alternate, 
                                      @QueryParam("effects") String effects,
                                      @QueryParam("studies") String studies,
                                      @DefaultValue("-1f") @QueryParam("maf") float maf,
                                      @DefaultValue("-1") @QueryParam("miss_alleles") int missingAlleles,
                                      @DefaultValue("-1") @QueryParam("miss_gts") int missingGenotypes,
                                      @DefaultValue("=") @QueryParam("maf_op") String mafOperator,
                                      @DefaultValue("=") @QueryParam("miss_alleles_op") String missingAllelesOperator,
                                      @DefaultValue("=") @QueryParam("miss_gts_op") String missingGenotypesOperator,
                                      @DefaultValue("") @QueryParam("type") String variantType) {
        if (reference != null) {
            queryOptions.put("reference", reference);
        }
        if (alternate != null) {
            queryOptions.put("alternate", alternate);
        }
        if (effects != null) {
            queryOptions.put("effect", Arrays.asList(effects.split(",")));
        }
        if (studies != null) {
            queryOptions.put("studies", Arrays.asList(studies.split(",")));
        }
        if (!variantType.isEmpty()) {
            queryOptions.put("type", variantType);
        }
        if (maf >= 0) {
            queryOptions.put("maf", maf);
            if (mafOperator != null) {
                queryOptions.put("opMaf", mafOperator);
            }
        }
        if (missingAlleles >= 0) {
            queryOptions.put("missingAlleles", missingAlleles);
            if (missingAllelesOperator != null) {
                queryOptions.put("opMissingAlleles", missingAllelesOperator);
            }
        }
        if (missingGenotypes >= 0) {
            queryOptions.put("missingGenotypes", missingGenotypes);
            if (missingGenotypesOperator != null) {
                queryOptions.put("opMissingGenotypes", missingGenotypesOperator);
            }
        }
        
        return createOkResponse(variantMongoQueryBuilder.getAllVariantsByGene(geneId, queryOptions));
    }
    
    @GET
    @Path("/ranking")
    public Response genesRankingByVariantsNumber(@PathParam("gene") String geneId,
                                                 @DefaultValue("10") @QueryParam("limit") int limit,
                                                 @DefaultValue("desc") @QueryParam("sort") String sort,
                                                 @DefaultValue("") @QueryParam("type") String variantType) {
        if (!variantType.isEmpty()) {
            queryOptions.put("type", variantType);
        }
        
        if (sort.equalsIgnoreCase("desc")) {
            return createOkResponse(variantMongoQueryBuilder.getMostAffectedGenes(limit, queryOptions));
        } else if (sort.equalsIgnoreCase("asc")) {
            return createOkResponse(variantMongoQueryBuilder.getLeastAffectedGenes(limit, queryOptions));
        } else {
            return createOkResponse("Sorting criteria must be 'desc' or 'asc'");
        }
    }
    
    
}
