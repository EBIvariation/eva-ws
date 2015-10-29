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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Splitter;
import io.swagger.annotations.ApiParam;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.opencb.biodata.models.feature.Genotype;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.VariantSourceEntry;
import org.opencb.biodata.models.variant.stats.VariantStats;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.opencga.storage.core.variant.io.json.GenotypeJsonMixin;
import org.opencb.opencga.storage.core.variant.io.json.VariantSourceEntryJsonMixin;
import org.opencb.opencga.storage.core.variant.io.json.VariantSourceJsonMixin;
import org.opencb.opencga.storage.core.variant.io.json.VariantStatsJsonMixin;
import org.opencb.opencga.storage.core.variant.io.json.VariantStatsJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.variation.eva.server.exception.SpeciesException;
import uk.ac.ebi.variation.eva.server.exception.VersionException;

/**
 * Created by imedina on 01/04/14.
 */
@Produces(MediaType.APPLICATION_JSON)
public class EvaWSServer {

    protected final String version = "v1";

    @DefaultValue("Homo sapiens")
    @QueryParam("species")
    @ApiParam(name = "species", value = "Excluded fields will not be returned. Comma separated JSON paths must be provided",
            defaultValue = "hsapiens", allowableValues = "hsapiens,mmusculus")
    protected String species;

    protected UriInfo uriInfo;
    protected HttpServletRequest httpServletRequest;

    protected QueryOptions queryOptions;
    protected QueryResponse queryResponse;
    protected long startTime;
    protected long endTime;

    @DefaultValue("")
    @QueryParam("exclude")
    @ApiParam(name = "excluded fields", value = "Excluded fields will not be returned. Comma separated JSON paths must be provided")
    protected String exclude;

    @DefaultValue("")
    @QueryParam("include")
    @ApiParam(name = "included fields", value = "Included fields are the only to be returned. Comma separated JSON path must be provided")
    protected String include;

    @DefaultValue("-1")
    @QueryParam("limit")
    @ApiParam(name = "limit", value = "Max number of results to be returned. No limit applied when -1 [-1]")
    protected int limit;

    @DefaultValue("-1")
    @QueryParam("skip")
    @ApiParam(name = "skip", value = "Number of results to be skipped. No skip applied when -1 [-1]")
    protected int skip;

    @DefaultValue("false")
    @QueryParam("count")
    @ApiParam(name = "count", value = "The total number of results is returned [false]")
    protected String count;

    protected static ObjectMapper jsonObjectMapper;
    protected static ObjectWriter jsonObjectWriter;

    protected static Logger logger;
    

    static {
        logger = LoggerFactory.getLogger(EvaWSServer.class);

        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.addMixInAnnotations(VariantSourceEntry.class, VariantSourceEntryJsonMixin.class);
        jsonObjectMapper.addMixInAnnotations(Genotype.class, GenotypeJsonMixin.class);
        jsonObjectMapper.addMixInAnnotations(VariantStats.class, VariantStatsJsonMixin.class);
        jsonObjectMapper.addMixInAnnotations(VariantSource.class, VariantSourceJsonMixin.class);
        jsonObjectMapper.setSerializationInclusion(Include.NON_NULL);
        
        SimpleModule module = new SimpleModule();
        module.addSerializer(VariantStats.class, new VariantStatsJsonSerializer());
        jsonObjectMapper.registerModule(module);
        
        jsonObjectWriter = jsonObjectMapper.writer();
    }

    @Deprecated
    public EvaWSServer() {
        logger.info("EvaWSServer: in 'constructor'");
    }

    public EvaWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest hsr) {
        this.uriInfo = uriInfo;
        this.httpServletRequest = hsr;

        this.startTime = System.currentTimeMillis();
        this.queryResponse = new QueryResponse();
        this.queryOptions = new QueryOptions();

        logger.info("EvaWSServer: in 'constructor'");
    }

    protected void checkParams() throws VersionException, SpeciesException {
        // TODO A Version and Species checker must be implemented
        if (version == null || !version.equals("v1")) {
            throw new VersionException("Version not valid: '" + version + "'");
        }
        if (species == null || species.isEmpty()/*|| !isValidSpecies(species)*/) {
            throw new SpeciesException("Species not valid: '" + species + "'");
        }

        MultivaluedMap<String, String> multivaluedMap = uriInfo.getQueryParameters();
        queryOptions.put("species", species);

        queryOptions.put("metadata", (multivaluedMap.get("metadata") != null) ? multivaluedMap.get("metadata").get(0).equals("true") : true);
        queryOptions.put("exclude", (exclude != null && !exclude.equals("")) ? Splitter.on(",").splitToList(exclude) : null);
        queryOptions.put("include", (include != null && !include.equals("")) ? Splitter.on(",").splitToList(include) : null);
        queryOptions.put("limit", (limit > 0) ? limit : -1);
        queryOptions.put("skip", (skip > 0) ? skip : -1);
        queryOptions.put("count", (count != null && !count.equals("")) ? Boolean.parseBoolean(count) : false);
    }

    protected Response createOkResponse(Object obj) {
        endTime = System.currentTimeMillis() - startTime;
        queryResponse.setTime(new Long(endTime - startTime).intValue());
        queryResponse.setApiVersion(version);
        queryResponse.setQueryOptions(queryOptions);
        
        // Guarantee that the QueryResponse object contains a coll of results
        List coll;
        if (obj instanceof List) {
            coll = (List) obj;
        } else {
            coll = new ArrayList();
            coll.add(obj);
        }
        queryResponse.setResponse(coll);

        try {
            return Response.ok(jsonObjectWriter.writeValueAsString(queryResponse), MediaType.APPLICATION_JSON_TYPE).build();
        } catch (JsonProcessingException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex).build();
        }
    }

    protected Response createUserErrorResponse(Object obj) {
        endTime = System.currentTimeMillis() - startTime;
        queryResponse.setTime(new Long(endTime - startTime).intValue());
        queryResponse.setApiVersion(version);
        queryResponse.setQueryOptions(queryOptions);
        queryResponse.setError(obj.toString());
        
        return Response.status(Response.Status.BAD_REQUEST).entity(queryResponse).build();
    }
    
    protected Response createErrorResponse(Object obj) {
        endTime = System.currentTimeMillis() - startTime;
        queryResponse.setTime(new Long(endTime - startTime).intValue());
        queryResponse.setApiVersion(version);
        queryResponse.setQueryOptions(queryOptions);
        queryResponse.setError(obj.toString());
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(queryResponse).build();
    }

    protected Response createJsonResponse(Object object) {
        return Response.ok(object).build();
    }
    
    protected Response createJsonUserErrorResponse(Object object) {
        return Response.status(Response.Status.BAD_REQUEST).entity(object).build();
    }
    
    protected Response createJsonErrorResponse(Object object) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(object).build();
    }
    
//    private Response buildResponse(Response.ResponseBuilder responseBuilder) {
//        return responseBuilder.header("Access-Control-Allow-Origin", "*")
//                .header("Access-Control-Allow-Headers", "x-requested-with, content-type, accept")
//                .header("Access-Control-Allow-Methods", "POST, GET, OPTIONS").build();
//    }
    
}
