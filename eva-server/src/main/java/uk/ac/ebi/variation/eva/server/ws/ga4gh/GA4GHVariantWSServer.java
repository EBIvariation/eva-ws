package uk.ac.ebi.variation.eva.server.ws.ga4gh;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.ga4gh.GASearchVariantRequest;
import org.opencb.biodata.ga4gh.GASearchVariantsResponse;
import org.opencb.biodata.ga4gh.GAVariant;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.ga4gh.GAVariantFactory;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.variant.VariantDBAdaptor;
import uk.ac.ebi.variation.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.variation.eva.server.ws.EvaWSServer;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Path("/{version}/ga4gh/variants")
@Produces(MediaType.APPLICATION_JSON)
public class GA4GHVariantWSServer extends EvaWSServer {
    
    public GA4GHVariantWSServer() {
        super();
    }

    public GA4GHVariantWSServer(@DefaultValue("") @PathParam("version")String version, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) {
        super(version, uriInfo, hsr);
    }

    @GET
    @Path("/search")
    /**
     * "start" and "end" are 0-based, whereas all the position stored are 1-based
     * 
     * @see http://ga4gh.org/documentation/api/v0.5/ga4gh_api.html#/schema/org.ga4gh.GASearchVariantsRequest
     */
    public Response getVariantsByRegion(@QueryParam("referenceName") String chromosome,
                                        @QueryParam("start") int start,
                                        @QueryParam("end") int end,
//                                        @QueryParam("variantName") String id,
                                        @QueryParam("variantSetIds") String files,
//                                        @QueryParam("callSetIds") String samples,
                                        @QueryParam("pageToken") String pageToken,
                                        @DefaultValue("10") @QueryParam("pageSize") int limit,
                                        @DefaultValue("false") @QueryParam("histogram") boolean histogram,
                                        @DefaultValue("-1") @QueryParam("histogram_interval") int interval) 
            throws IllegalOpenCGACredentialsException, UnknownHostException, IOException {
        
        VariantDBAdaptor variantMongoDbAdaptor = DBAdaptorConnector.getVariantDBAdaptor("hsapiens");
        
        if (files != null && !files.isEmpty()) {
            queryOptions.put("files", Arrays.asList(files.split(",")));
        }
        
        int idxCurrentPage = 0;
        if (pageToken != null && !pageToken.isEmpty() && StringUtils.isNumeric(pageToken)) {
            idxCurrentPage = Integer.parseInt(pageToken);
            queryOptions.put("skip", idxCurrentPage * limit);
        }
        queryOptions.put("limit", limit);
        
        // Create the provided region, whose size can't excede 1 million positions
        Region region = new Region(chromosome, start, end);
        int regionSize = region.getEnd()-region.getStart();
        
        if (histogram) {
            if (interval > 0) {
                queryOptions.put("interval", interval);
            }
            return createOkResponse(variantMongoDbAdaptor.getVariantsHistogramByRegion(region, queryOptions));
        } else if (regionSize <= 1000000) {
            QueryResult<Variant> qr = variantMongoDbAdaptor.getAllVariantsByRegion(region, queryOptions);
            // Convert Variant objects to GAVariant
            List<GAVariant> gaVariants = GAVariantFactory.create(qr.getResult());
            // Calculate the next page token
            int idxLastElement = idxCurrentPage * limit + limit;
            String nextPageToken = (idxLastElement < qr.getNumTotalResults()) ? String.valueOf(idxCurrentPage + 1) : null;
            
            // Create the custom response for the GA4GH API
            return createJsonResponse(new GASearchVariantsResponse(gaVariants, nextPageToken));
        } else {
            return createErrorResponse("The total size of all regions provided can't exceed 1 million positions. "
                    + "If you want to browse a larger number of positions, please provide the parameter 'histogram=true'");
        }
        
    }
    
    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getVariantsByRegion(GASearchVariantRequest request) 
            throws IllegalOpenCGACredentialsException, UnknownHostException, IOException {
        request.validate();
        return getVariantsByRegion(request.getReferenceName(), (int) request.getStart(), (int) request.getEnd(), 
                   StringUtils.join(request.getVariantSetIds(), ","), request.getPageToken(), request.getPageSize(), 
                   false, -1);
    }
    
}
