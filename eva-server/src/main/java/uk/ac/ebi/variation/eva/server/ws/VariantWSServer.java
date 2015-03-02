package uk.ac.ebi.variation.eva.server.ws;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.opencb.biodata.models.feature.Region;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
import uk.ac.ebi.variation.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.variation.eva.server.exception.SpeciesException;
import uk.ac.ebi.variation.eva.server.exception.VersionException;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Path("/{version}/variants")
@Produces("application/json")
@Api(value = "Variant", description = "Variant RESTful Web Services API")
public class VariantWSServer extends EvaWSServer {


    public VariantWSServer(@DefaultValue("") @PathParam("version")String version,
                           @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws IOException {
        super(version, uriInfo, hsr);
    }

    @GET
    @Path("/{variantId}/info")
    @ApiOperation(httpMethod = "GET", value = "Retrieves the info from a list of variants", response = QueryResponse.class)
    public Response getVariantById(@PathParam("variantId") String variantId,
                                   @QueryParam("studies") String studies,
                                   @QueryParam("species") String species) 
            throws IllegalOpenCGACredentialsException, UnknownHostException, IOException {
        try {
            checkParams();
        } catch (VersionException | SpeciesException ex) {
            return createErrorResponse(ex.toString());
        }
        
        VariantDBAdaptor variantMongoDbAdaptor = DBAdaptorConnector.getVariantDBAdaptor(species);
        
        if (studies != null && !studies.isEmpty()) {
            queryOptions.put("studies", Arrays.asList(studies.split(",")));
        }
        
        if (!variantId.contains(":")) { // Query by accession id
            return createOkResponse(variantMongoDbAdaptor.getVariantById(variantId, queryOptions));
        } else { // Query by chr:pos:ref:alt
            String parts[] = variantId.split(":", -1);
            if (parts.length < 3) {
                return createErrorResponse("Invalid position and alleles combination, please use chr:pos:ref or chr:pos:ref:alt");
            }

            Region region = new Region(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[1]));
            queryOptions.put("reference", parts[2]);
            if (parts.length > 3) {
                queryOptions.put("alternate", parts[3]);
            }

            return createOkResponse(variantMongoDbAdaptor.getAllVariantsByRegion(region, queryOptions));
        }
    }

    @GET
    @Path("/{variantId}/exists")
    @ApiOperation(httpMethod = "GET", value = "Find if a list of variants exist", response = QueryResponse.class)
    public Response checkVariantExists(@PathParam("variantId") String variantId,
                                       @QueryParam("studies") String studies,
                                       @QueryParam("species") String species) 
            throws IllegalOpenCGACredentialsException, UnknownHostException, IOException {
        try {
            checkParams();
        } catch (VersionException | SpeciesException ex) {
            return createErrorResponse(ex.toString());
        }
        
        VariantDBAdaptor variantMongoDbAdaptor = DBAdaptorConnector.getVariantDBAdaptor(species);
        
        if (studies != null && !studies.isEmpty()) {
            queryOptions.put("studies", Arrays.asList(studies.split(",")));
        }
        
        if (!variantId.contains(":")) { // Query by accession id
            return createErrorResponse("Invalid position and alleles combination, please use chr:pos:ref or chr:pos:ref:alt");
        } else { // Query by chr:pos:ref:alt
            String parts[] = variantId.split(":", -1);
            if (parts.length < 3) {
                return createErrorResponse("Invalid position and alleles combination, please use chr:pos:ref or chr:pos:ref:alt");
            }

            Region region = new Region(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[1]));
            queryOptions.put("reference", parts[2]);
            if (parts.length > 3) {
                queryOptions.put("alternate", parts[3]);
            }

            QueryResult queryResult = variantMongoDbAdaptor.getAllVariantsByRegion(region, queryOptions);
            queryResult.setResult(Arrays.asList(queryResult.getNumResults() > 0));
            queryResult.setResultType(Boolean.class.getCanonicalName());
            return createOkResponse(queryResult);
        }
    }

}
