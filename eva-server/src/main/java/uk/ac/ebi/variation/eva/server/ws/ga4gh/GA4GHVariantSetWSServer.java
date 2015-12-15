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

package uk.ac.ebi.variation.eva.server.ws.ga4gh;

import io.swagger.annotations.Api;
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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.ga4gh.GASearchVariantSetsRequest;
import org.opencb.biodata.ga4gh.GASearchVariantSetsResponse;
import org.opencb.biodata.ga4gh.GAVariantSet;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.ga4gh.GAVariantSetFactory;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantSourceDBAdaptor;
import uk.ac.ebi.variation.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.variation.eva.server.ws.EvaWSServer;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Path("/v1/ga4gh/variantsets")
@Produces(MediaType.APPLICATION_JSON)
@Api(tags = { "ga4gh", "files" })
public class GA4GHVariantSetWSServer extends EvaWSServer {
    
    public GA4GHVariantSetWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest hsr) {
        super(uriInfo, hsr);
    }

    @GET
    @Path("/search")
    /**
     * 
     * @see http://ga4gh.org/documentation/api/v0.5/ga4gh_api.html#/schema/org.ga4gh.GASearchVariantSetsRequest
     */
    public Response getVariantSets(@QueryParam("datasetIds") String studies,
                                   @QueryParam("pageToken") String pageToken,
                                   @DefaultValue("10") @QueryParam("pageSize") int limit,
                                   @DefaultValue("false") @QueryParam("histogram") boolean histogram,
                                   @DefaultValue("-1") @QueryParam("histogram_interval") int interval)
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        
        if (studies == null || studies.isEmpty()) {
            return createJsonUserErrorResponse("The 'datasetIds' argument must not be empty");
        }
        
        VariantSourceDBAdaptor dbAdaptor = DBAdaptorConnector.getVariantSourceDBAdaptor("hsapiens_grch37");
        
        int idxCurrentPage = 0;
        if (pageToken != null && !pageToken.isEmpty() && StringUtils.isNumeric(pageToken)) {
            idxCurrentPage = Integer.parseInt(pageToken);
            queryOptions.put("skip", idxCurrentPage * limit);
        }
        queryOptions.put("limit", limit);
        
        List<String> studiesList = Arrays.asList(studies.split(","));
        QueryResult<VariantSource> qr;
        if (studiesList.isEmpty()) {
            qr = dbAdaptor.getAllSources(queryOptions);
        } else {
            qr = dbAdaptor.getAllSourcesByStudyIds(studiesList, queryOptions);
        }
        
        // Convert VariantSource objects to GAVariantSet
        List<GAVariantSet> gaVariantSets = GAVariantSetFactory.create(qr.getResult());
        // Calculate the next page token
        int idxLastElement = idxCurrentPage * limit + limit;
        String nextPageToken = (idxLastElement < qr.getNumTotalResults()) ? String.valueOf(idxCurrentPage + 1) : null;

        // Create the custom response for the GA4GH API
        return createJsonResponse(new GASearchVariantSetsResponse(gaVariantSets, nextPageToken));
    }
    
    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getVariantSets(GASearchVariantSetsRequest request) 
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        return getVariantSets(StringUtils.join(request.getDatasetIds(), ','), 
                request.getPageToken(), request.getPageSize(), false, -1);
    }
    
}
