/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014, 2015 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.variation.eva.server.ws;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.Api;
import org.opencb.biodata.models.feature.Region;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
import uk.ac.ebi.variation.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.variation.eva.server.exception.SpeciesException;
import uk.ac.ebi.variation.eva.server.exception.VersionException;

/**
 * Created by imedina on 01/04/14.
 */
@Path("/v1/segments")
@Produces("application/json")
@Api(tags = { "segments" })
public class RegionWSServer extends EvaWSServer {


    public RegionWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws IOException {
        super(uriInfo, hsr);
    }

    @GET
    @Path("/{region}/variants")
//    @ApiOperation(httpMethod = "GET", value = "Retrieves all the variants from region", response = QueryResponse.class)
    public Response getVariantsByRegion(@PathParam("region") String regionId,
                                        @QueryParam("ref") String reference,
                                        @QueryParam("alt") String alternate,
                                        @QueryParam("species") String species,
                                        @DefaultValue("") @QueryParam("miss_alleles") String missingAlleles,
                                        @DefaultValue("") @QueryParam("miss_gts") String missingGenotypes,
                                        @DefaultValue("false") @QueryParam("histogram") boolean histogram,
                                        @DefaultValue("-1") @QueryParam("histogram_interval") int interval,
                                        @DefaultValue("false") @QueryParam("merge") boolean merge) 
            throws IllegalOpenCGACredentialsException, IOException {
        try {
            checkParams();
        } catch (VersionException | SpeciesException ex) {
            return createErrorResponse(ex.toString());
        }

        VariantDBAdaptor variantMongoDbAdaptor = DBAdaptorConnector.getVariantDBAdaptor(species);

        for (String acceptedValue : VariantDBAdaptor.QueryParams.acceptedValues) {
            if (uriInfo.getQueryParameters().containsKey(acceptedValue)) {
                List<String> values = uriInfo.getQueryParameters(true).get(acceptedValue);
                String csv = values.get(0);
                for (int i = 1; i < values.size(); i++) {
                    csv += "," + values.get(i);
                }
                queryOptions.add(acceptedValue, csv);
            }
        }

        if (reference != null && !reference.isEmpty()) {
            queryOptions.put("reference", reference);
        }
        if (alternate != null && !alternate.isEmpty()) {
            queryOptions.put("alternate", alternate);
        }
        if (!missingAlleles.isEmpty()) {
            queryOptions.put("missingAlleles", missingAlleles);
        }
        if (!missingGenotypes.isEmpty()) {
            queryOptions.put("missingGenotypes", missingGenotypes);
        }

        queryOptions.put("merge", merge);
        queryOptions.put("sort", true);

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
            if (regions.size() != 1) {
                return createErrorResponse("Sorry, histogram functionality only works with a single region");
            } else {
                if (interval > 0) {
                    queryOptions.put("interval", interval);
                }
                return createOkResponse(variantMongoDbAdaptor.getVariantFrequencyByRegion(regions.get(0), queryOptions));
            }
        } else if (regionsSize <= 1000000) {
            if (regions.isEmpty()) {
                if (!queryOptions.containsKey("id") && !queryOptions.containsKey("gene")) {
                    return createErrorResponse("Some positional filer is needed, like region, gene or id.");
                } else {
                    return createOkResponse(variantMongoDbAdaptor.getAllVariants(queryOptions));
                }
            } else {
                return createOkResponse(variantMongoDbAdaptor.getAllVariantsByRegionList(regions, queryOptions));
            }
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
