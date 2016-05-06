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

import io.swagger.annotations.Api;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.opencb.biodata.models.feature.Region;
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
@Path("/v1/variants")
@Produces("application/json")
@Api(tags = { "variants" })
public class VariantWSServer extends EvaWSServer {


    public VariantWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws IOException {
        super(uriInfo, hsr);
    }

    @GET
    @Path("/{variantId}/info")
//    @ApiOperation(httpMethod = "GET", value = "Retrieves the information about a variant", response = QueryResponse.class)
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
                queryOptions.put("alternate", String.join(":", Arrays.copyOfRange(parts, 3, parts.length)));
            }

            return createOkResponse(variantMongoDbAdaptor.getAllVariantsByRegion(region, queryOptions));
        }
    }

    @GET
    @Path("/{variantId}/exists")
//    @ApiOperation(httpMethod = "GET", value = "Checks if a variants exist", response = QueryResponse.class)
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
