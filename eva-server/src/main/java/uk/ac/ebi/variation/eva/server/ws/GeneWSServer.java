package uk.ac.ebi.variation.eva.server.ws;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import java.util.List;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
import uk.ac.ebi.variation.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.variation.eva.server.exception.SpeciesException;
import uk.ac.ebi.variation.eva.server.exception.VersionException;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Path("/v1/genes")
@Produces("application/json")
@Api(tags = { "genes" })
public class GeneWSServer extends EvaWSServer {


    public GeneWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest hsr) {
        super(uriInfo, hsr);
    }

    @GET
    @Path("/{gene}/variants")
//    @ApiOperation(httpMethod = "GET", value = "Retrieves all the variants of a gene", response = QueryResponse.class)
    public Response getVariantsByGene(@PathParam("gene") String geneId,
                                      @QueryParam("ref") String reference,
                                      @QueryParam("alt") String alternate, 
                                      @QueryParam("effects") String effects,
                                      @QueryParam("studies") String studies,
                                      @QueryParam("species") String species,
                                      @DefaultValue("-1f") @QueryParam("maf") float maf,
                                      @DefaultValue("-1") @QueryParam("miss_alleles") int missingAlleles,
                                      @DefaultValue("-1") @QueryParam("miss_gts") int missingGenotypes,
                                      @DefaultValue("=") @QueryParam("maf_op") String mafOperator,
                                      @DefaultValue("=") @QueryParam("miss_alleles_op") String missingAllelesOperator,
                                      @DefaultValue("=") @QueryParam("miss_gts_op") String missingGenotypesOperator,
                                      @DefaultValue("") @QueryParam("type") String variantType)
            throws IllegalOpenCGACredentialsException, UnknownHostException, IOException {
        try {
            checkParams();
        } catch (VersionException | SpeciesException ex) {
            return createErrorResponse(ex.toString());
        }
        
        VariantDBAdaptor variantMongoDbAdaptor = DBAdaptorConnector.getVariantDBAdaptor(species);
        
        for (String acceptedValue : VariantDBAdaptor.QueryParams.acceptedValues) {
            if (uriInfo.getQueryParameters().containsKey(acceptedValue)) {
                List<String> values = uriInfo.getQueryParameters().get(acceptedValue);
                String csv = values.get(0);
                for (int i = 1; i < values.size(); i++) {
                    csv += "," + values.get(i);
                }
                queryOptions.add(acceptedValue, csv);
            }
        }

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

        return createOkResponse(variantMongoDbAdaptor.getAllVariantsByGene(geneId, queryOptions));
    }
    
}
