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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Splitter;

import io.swagger.annotations.ApiParam;

/**
 * Created by imedina on 01/04/14.
 */
public class EvaWSServer {

    protected final String version = "v1";

    protected UriInfo uriInfo;
    
    @Autowired
    protected HttpServletRequest httpServletRequest;

    protected QueryOptions queryOptions;
    protected long startTime;
    protected long endTime;

    protected static ObjectMapper jsonObjectMapper;
    protected static ObjectWriter jsonObjectWriter;

    protected static Logger logger;
    

    static {
        logger = LoggerFactory.getLogger(EvaWSServer.class);

        jsonObjectMapper = new ObjectMapper()
        		.addMixIn(VariantSourceEntry.class, VariantSourceEntryJsonMixin.class)
        		.addMixIn(Genotype.class, GenotypeJsonMixin.class)
        		.addMixIn(VariantStats.class, VariantStatsJsonMixin.class)
        		.addMixIn(VariantSource.class, VariantSourceJsonMixin.class)
        		.setSerializationInclusion(Include.NON_NULL);
        
        SimpleModule module = new SimpleModule();
        module.addSerializer(VariantStats.class, new VariantStatsJsonSerializer());
        jsonObjectMapper.registerModule(module);
        
        jsonObjectWriter = jsonObjectMapper.writer();
    }

    public EvaWSServer() { }

    public EvaWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest hsr) {
        this.uriInfo = uriInfo;
        this.httpServletRequest = hsr;

        this.startTime = System.currentTimeMillis();
        this.queryOptions = new QueryOptions();

        logger.info("EvaWSServer: in 'constructor'");
    }

    protected void checkParams() {
        this.queryOptions = new QueryOptions();
        Map<String, String[]> multivaluedMap = httpServletRequest.getParameterMap();
        
        boolean metadata = (multivaluedMap.get("metadata") != null) ? multivaluedMap.get("metadata")[0].equals("true") : true ;
        int limit = (multivaluedMap.get("limit") != null) ? Integer.parseInt(multivaluedMap.get("limit")[0]) : -1;
        int skip = (multivaluedMap.get("skip") != null) ? Integer.parseInt(multivaluedMap.get("skip")[0]) : -1;
        boolean count = (multivaluedMap.get("count") != null) ? multivaluedMap.get("count")[0].equals("true") : false ;

        String[] exclude = multivaluedMap.get("exclude");
        String[] include = multivaluedMap.get("include");
        
        queryOptions.put("metadata", metadata);
        queryOptions.put("exclude", exclude);
        queryOptions.put("include", include);
        queryOptions.put("limit", (limit > 0) ? limit : -1);
        queryOptions.put("skip", (skip > 0) ? skip : -1);
        queryOptions.put("count", count);
        System.out.println(queryOptions.toJson());
    }

    protected QueryResponse setQueryResponse(Object obj) {
        QueryResponse queryResponse = new QueryResponse();
    	endTime = System.currentTimeMillis() - startTime;
    	// TODO Restore span time calculation
//        queryResponse.setTime(new Long(endTime - startTime).intValue());
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
        
        return queryResponse;
    }
    
    protected Response createOkResponse(Object obj) {
        QueryResponse queryResponse = setQueryResponse(obj);

        try {
            return Response.ok(jsonObjectWriter.writeValueAsString(queryResponse), MediaType.APPLICATION_JSON_TYPE).build();
        } catch (JsonProcessingException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex).build();
        }
    }

    protected Response createUserErrorResponse(Object obj) {
        QueryResponse queryResponse = setQueryResponse(obj);
        endTime = System.currentTimeMillis() - startTime;
        queryResponse.setTime(new Long(endTime - startTime).intValue());
        queryResponse.setApiVersion(version);
        queryResponse.setQueryOptions(queryOptions);
        queryResponse.setError(obj.toString());
        
        return Response.status(Response.Status.BAD_REQUEST).entity(queryResponse).build();
    }
    
    protected Response createErrorResponse(Object obj) {
        QueryResponse queryResponse = setQueryResponse(obj);
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
