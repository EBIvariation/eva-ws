/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014-2016 EMBL - European Bioinformatics Institute
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.opencb.biodata.models.feature.Region;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import uk.ac.ebi.variation.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.variation.eva.server.exception.SpeciesException;
import uk.ac.ebi.variation.eva.server.exception.VersionException;

/**
 * Created by imedina on 01/04/14.
 */
// @Path("/v1/segments")
// @Produces("application/json")
@RestController
@RequestMapping("/v1/segments")
@Api(tags = { "segments" })
public class RegionWSServer extends EvaWSServer {

    public RegionWSServer() {
    }

    public RegionWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws IOException {
        super(uriInfo, hsr);
    }

    @RequestMapping(value = "/{regionId}/variants", method = RequestMethod.GET)
    @ResponseBody
//    @ApiOperation(httpMethod = "GET", value = "Retrieves all the variants from region", response = QueryResponse.class)
    // TODO Add other parameters that are currently hidden
    // TODO Exception handling
    public QueryResponse getVariantsByRegion(@PathVariable("regionId") String regionId,
                                             @RequestParam(name = "ref", required = false) String reference,
                                             @RequestParam(name = "alt", required = false) String alternate,
                                             @RequestParam(name = "species") String species,
                                             @RequestParam(name = "miss_alleles", defaultValue = "") String missingAlleles,
                                             @RequestParam(name = "miss_gts", defaultValue = "") String missingGenotypes,
                                             @RequestParam(name = "histogram", defaultValue = "false") boolean histogram,
                                             @RequestParam(name = "histogram_interval", defaultValue = "-1") int interval,
                                             @RequestParam(name = "merge", defaultValue = "false") boolean merge)
            throws IllegalOpenCGACredentialsException, IOException {
        checkParams();

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
                return setQueryResponse("Sorry, histogram functionality only works with a single region");
            } else {
                if (interval > 0) {
                    queryOptions.put("interval", interval);
                }
                return setQueryResponse(
                        variantMongoDbAdaptor.getVariantFrequencyByRegion(regions.get(0), queryOptions));

            }
        } else if (regionsSize <= 1000000) {
            if (regions.isEmpty()) {
                if (!queryOptions.containsKey("id") && !queryOptions.containsKey("gene")) {
                    return setQueryResponse("Some positional filer is needed, like region, gene or id.");
                } else {
                    return setQueryResponse(variantMongoDbAdaptor.getAllVariants(queryOptions));
                }
            } else {
                return setQueryResponse(variantMongoDbAdaptor.getAllVariantsByRegionList(regions, queryOptions));
            }
        } else {
            return setQueryResponse("The total size of all regions provided can't exceed 1 million positions. "
                    + "If you want to browse a larger number of positions, please provide the parameter 'histogram=true'");
        }
    }

    @RequestMapping(value = "/{regionId}/variants", method = RequestMethod.OPTIONS)
    public Response getVariantsByRegion() {
        return createOkResponse("");
    }
}
