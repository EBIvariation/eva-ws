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
                                      @QueryParam("species") String species,
                                      @DefaultValue("") @QueryParam("miss_alleles") String missingAlleles,
                                      @DefaultValue("") @QueryParam("miss_gts") String missingGenotypes,
                                      @DefaultValue("") @QueryParam("type") String variantType)
            throws IllegalOpenCGACredentialsException, UnknownHostException, IOException {
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

        if (reference != null) {
            queryOptions.put("reference", reference);
        }
        if (alternate != null) {
            queryOptions.put("alternate", alternate);
        }
        if (!variantType.isEmpty()) {
            queryOptions.put("type", variantType);
        }
        
        queryOptions.put("sort", true);

        return createOkResponse(variantMongoDbAdaptor.getAllVariantsByGene(geneId, queryOptions));
    }
    
}
