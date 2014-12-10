package uk.ac.ebi.variation.eva.server.ws;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opencb.biodata.models.feature.Region;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.variant.VariantDBAdaptor;
import uk.ac.ebi.variation.eva.lib.datastore.DBAdaptorConnector;

/**
 * Created by imedina on 01/04/14.
 */
@Path("/{version}/segments")
@Produces(MediaType.APPLICATION_JSON)
public class RegionWSServer extends EvaWSServer {

    public RegionWSServer() {
        super();
    }

    public RegionWSServer(@DefaultValue("") @PathParam("version")String version, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) 
            throws IOException {
        super(version, uriInfo, hsr);
    }

    @GET
    @Path("/{region}/variants")
    public Response getVariantsByRegion(@PathParam("region") String regionId,
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
                                        @DefaultValue("") @QueryParam("type") String variantType,
                                        @DefaultValue("false") @QueryParam("histogram") boolean histogram,
                                        @DefaultValue("-1") @QueryParam("histogram_interval") int interval,
                                        @DefaultValue("false") @QueryParam("merge") boolean merge) 
            throws IllegalOpenCGACredentialsException, UnknownHostException, IOException {
        
        VariantDBAdaptor variantMongoDbAdaptor = DBAdaptorConnector.getVariantDBAdaptor(species);
        
        if (reference != null && !reference.isEmpty()) {
            queryOptions.put("reference", reference);
        }
        if (alternate != null && !alternate.isEmpty()) {
            queryOptions.put("alternate", alternate);
        }
        if (effects != null && !effects.isEmpty()) {
            queryOptions.put("effect", Arrays.asList(effects.split(",")));
        }
        if (studies != null && !studies.isEmpty()) {
            queryOptions.put("studies", Arrays.asList(studies.split(",")));
        }
        if (species != null && !species.isEmpty()) {
            queryOptions.put("species", species);
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
        queryOptions.put("merge", merge);
        
        // Parse the provided regions. The total size of all regions together 
        // can't excede 1 million positions
        int regionsSize = 0;
        List<Region> regions = new ArrayList<>();
        for (String s : regionId.split(",")) {
            Region r = Region.parseRegion(s);
            regions.add(r);
            regionsSize += r.getEnd() - r.getStart();
        }
        
        if (histogram) {
            if (regions.size() > 1) {
                return createErrorResponse("Sorry, histogram functionality only works with a single region");
            } else {
                if (interval > 0) {
                    queryOptions.put("interval", interval);
                }
                return createOkResponse(variantMongoDbAdaptor.getVariantsHistogramByRegion(regions.get(0), queryOptions));
            }
        } else if (regionsSize <= 1000000) {
            return createOkResponse(variantMongoDbAdaptor.getAllVariantsByRegionList(regions, queryOptions));
        } else {
            return createErrorResponse("The total size of all regions provided can't exceed 1 million positions. "
                    + "If you want to browse a larger number of positions, please provide the parameter 'histogram=true'");
        }
    }
    
    @OPTIONS
    @Path("/{region}/variants")
    public Response getVariantsByRegion() {
        return createOkResponse("");
    }
}
