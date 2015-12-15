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
    protected static Map<String, String> dict;
    protected static Map<Integer, String> erzDict;


    protected static Logger logger;
    

    static {
        logger = LoggerFactory.getLogger(EvaWSServer.class);
        logger.info("EvaWSServer: Initialising attributes inside static block");

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

        dict = new HashMap<>();
        dict.put("PRJEB4019", "8616");
        dict.put("PRJEB5439","2");
        dict.put("PRJEB8661","130");
        dict.put("PRJEB5829","156");
        dict.put("PRJEB6041","5385");
        dict.put("PRJEB6042","5404");
        dict.put("PRJEB7895","5423");
        dict.put("PRJEB6930","301");
        dict.put("PRJEB7218","5442");
        dict.put("PRJEB8705","5459");
        dict.put("PRJEB7217","5480");
        dict.put("PRJEB8652","5509");
        dict.put("PRJEB8650","5643");
        dict.put("PRJEB7923","5778");
        dict.put("PRJEB7894","6558");
        dict.put("PRJEB6911","8413");
        dict.put("PRJEB5473","8476");
        dict.put("PRJEB4395","8531");
        dict.put("PRJEB8639","11645");
        dict.put("PRJEB6025","12002");
        dict.put("PRJEB6495","12204");
        dict.put("PRJEB5978","12235");
        dict.put("PRJEB6057","12270");
        dict.put("PRJEB6119","12474");
        dict.put("PRJEB7061","12504");
        dict.put("PRJEB7723","12569");
        dict.put("PRJEB9507","33687");
        dict.put("PRJEB629","34064");

        erzDict.put(52, "ERZ017134");
        erzDict.put(8603, "ERZX00038");
        erzDict.put(34886, "ERZX00061");
        erzDict.put(208, "ERZ019953");
        erzDict.put(11768, "ERZ094141");
        erzDict.put(11795, "ERZ094140");
        erzDict.put(5552, "ERZ094203");
        erzDict.put(11727, "ERZ094147");
        erzDict.put(11724, "ERZ094137");
        erzDict.put(5558, "ERZ094199");
        erzDict.put(11523, "ERZ015357");
        erzDict.put(5554, "ERZ094211");
        erzDict.put(5560, "ERZ094202");
        erzDict.put(5577, "ERZ094190");
        erzDict.put(11507, "ERZ015346");
        erzDict.put(5683, "ERZ094176");
        erzDict.put(11535, "ERZ015350");
        erzDict.put(34888, "ERZX00059");
        erzDict.put(5700, "ERZ094177");
        erzDict.put(5545, "ERZ094208");
        erzDict.put(5572, "ERZ094193");
        erzDict.put(5691, "ERZ094167");
        erzDict.put(5676, "ERZ094168");
        erzDict.put(34858, "ERZX00067");
        erzDict.put(218, "ERZ019961");
        erzDict.put(5708, "ERZ094179");
        erzDict.put(5399, "ERZ038109");
        erzDict.put(8591, "ERZX00040");
        erzDict.put(5688, "ERZ094181");
        erzDict.put(11517, "ERZ015355");
        erzDict.put(56, "ERZ017133");
        erzDict.put(34866, "ERZX00060");
        erzDict.put(5722, "ERZ094189");
        erzDict.put(72, "ERZ017138");
        erzDict.put(8585, "ERZX00051");
        erzDict.put(230, "ERZ019952");
        erzDict.put(11745, "ERZ094131");
        erzDict.put(11713, "ERZ094144");
        erzDict.put(8611, "ERZX00042");
        erzDict.put(5437, "ERZ049522");
        erzDict.put(11705, "ERZ094135");
        erzDict.put(11756, "ERZ094138");
        erzDict.put(5695, "ERZ094180");
        erzDict.put(175, "ERZ019949");
        erzDict.put(34856, "ERZX00069");
        erzDict.put(216, "ERZ019947");
        erzDict.put(11758, "ERZ094146");
        erzDict.put(5673, "ERZ094188");
        erzDict.put(11742, "ERZ094136");
        erzDict.put(5706, "ERZ094175");
        erzDict.put(34923, "ERZX00058");
        erzDict.put(5549, "ERZ094192");
        erzDict.put(5418, "ERZX00026");
        erzDict.put(11466, "ERZ015359");
        erzDict.put(5716, "ERZ094171");
        erzDict.put(11469, "ERZ015345");
        erzDict.put(83, "ERZ017131");
        erzDict.put(203, "ERZ019944");
        erzDict.put(228, "ERZ019942");
        erzDict.put(70, "ERZ017132");
        erzDict.put(11485, "ERZ015362");
        erzDict.put(6550, "ERZ017128");
        erzDict.put(11810, "ERZ094152");
        erzDict.put(122, "ERZ017144");
        erzDict.put(11463, "ERZ015363");
        erzDict.put(5553, "ERZ094198");
        erzDict.put(24, "ERZ017137");
        erzDict.put(5581, "ERZ094194");
        erzDict.put(5380, "ERZX00049");
        erzDict.put(5590, "ERZ094201");
        erzDict.put(5477, "ERZ038104");
        erzDict.put(11723, "ERZ094145");
        erzDict.put(5692, "ERZ094169");
        erzDict.put(8593, "ERZX00046");
        erzDict.put(189, "ERZ019955");
        erzDict.put(5701, "ERZ094186");
        erzDict.put(5473, "ERZ097166");
        erzDict.put(11525, "ERZ015365");
        erzDict.put(185, "ERZX00006");
        erzDict.put(8597, "ERZX00039");
        erzDict.put(11489, "ERZ015352");
        erzDict.put(292, "ERZ019948");
        erzDict.put(5684, "ERZ094173");
        erzDict.put(5562, "ERZ094210");
        erzDict.put(8614, "ERZX00052");
        erzDict.put(11731, "ERZ094139");
        erzDict.put(35, "ERZ017123");
        erzDict.put(11491, "ERZ015353");
        erzDict.put(11477, "ERZ015358");
        erzDict.put(5709, "ERZ094185");
        erzDict.put(11529, "ERZ015356");
        erzDict.put(34870, "ERZX00070");
        erzDict.put(11701, "ERZ094151");
        erzDict.put(11452, "ERZX00033");
        erzDict.put(76, "ERZ017136");
        erzDict.put(8599, "ERZX00034");
        erzDict.put(34879, "ERZX00066");
        erzDict.put(34896, "ERZX00068");
        erzDict.put(8589, "ERZX00044");
        erzDict.put(8587, "ERZX00031");
        erzDict.put(294, "ERZ019962");
        erzDict.put(5680, "ERZ094174");
        erzDict.put(274, "ERZ019957");
        erzDict.put(8583, "ERZX00043");
        erzDict.put(110, "ERZ017140");
        erzDict.put(5598, "ERZ094191");
        erzDict.put(58, "ERZ017141");
        erzDict.put(11703, "ERZ094149");
        erzDict.put(34872, "ERZX00071");
        erzDict.put(34864, "ERZX00063");
        erzDict.put(5494, "ERZ038105");
        erzDict.put(34878, "ERZX00054");
        erzDict.put(5725, "ERZ094182");
        erzDict.put(11736, "ERZ094133");
        erzDict.put(5721, "ERZ094178");
        erzDict.put(11729, "ERZ094134");
        erzDict.put(11460, "ERZ015710");
        erzDict.put(34862, "ERZX00055");
        erzDict.put(60, "ERZ017126");
        erzDict.put(5743, "ERZ094172");
        erzDict.put(8605, "ERZX00045");
        erzDict.put(34876, "ERZX00076");
        erzDict.put(5719, "ERZ094166");
        erzDict.put(46, "ERZ017125");
        erzDict.put(5544, "ERZ094197");
        erzDict.put(34852, "ERZX00064");
        erzDict.put(54, "ERZ017142");
        erzDict.put(5579, "ERZ094196");
        erzDict.put(5382, "ERZX00035");
        erzDict.put(8601, "ERZX00037");
        erzDict.put(48, "ERZ017121");
        erzDict.put(34884, "ERZX00074");
        erzDict.put(11707, "ERZ094148");
        erzDict.put(11480, "ERZ015360");
        erzDict.put(280, "ERZ019951");
        erzDict.put(5506, "ERZX00050");
        erzDict.put(11482,"ERZ015354");
        erzDict.put(11712, "ERZ094132");
        erzDict.put(11501, "ERZ015349");
        erzDict.put(5584, "ERZ094213");
        erzDict.put(11511, "ERZ015368");
        erzDict.put(81, "ERZ017127");
        erzDict.put(187, "ERZ019943");
        erzDict.put(34894, "ERZX00075");
        erzDict.put(8609, "ERZX00036");
        erzDict.put(11753, "ERZ094129");
        erzDict.put(11760, "ERZ094143");
        erzDict.put(282, "ERZ019959");
        erzDict.put(11721, "ERZ094142");
        erzDict.put(5702, "ERZ094183");
        erzDict.put(5568, "ERZ094209");
        erzDict.put(5697, "ERZ094187");
        erzDict.put(5687, "ERZ094170");
        erzDict.put(41, "ERZ017143");
        erzDict.put(5588, "ERZ094212");
        erzDict.put(11455, "ERZX00048");
        erzDict.put(224, "ERZ019960");
        erzDict.put(5586, "ERZ094200");
        erzDict.put(30, "ERZ017135");
        erzDict.put(8607, "ERZX00032");
        erzDict.put(34882, "ERZX00072");
        erzDict.put(11734, "ERZ094130");
        erzDict.put(11519, "ERZ015347");
        erzDict.put(11717, "ERZ094150");
        erzDict.put(9861, "ERZX00041");
        erzDict.put(222, "ERZ019946");
        erzDict.put(5566, "ERZ094206");
        erzDict.put(210, "ERZ019956");
        erzDict.put(226, "ERZ019950");
        erzDict.put(34854, "ERZX00057");
        erzDict.put(37, "ERZ017129");
        erzDict.put(85, "ERZ017124");
        erzDict.put(6521, "ERZ017139");
        erzDict.put(34860, "ERZX00056");
        erzDict.put(278, "ERZ019954");
        erzDict.put(5712, "ERZ094184");
        erzDict.put(193, "ERZ019940");
        erzDict.put(11521, "ERZ015351");
        erzDict.put(34850, "ERZX00073");
        erzDict.put(27, "ERZ017130");
        erzDict.put(5564, "ERZ094207");
        erzDict.put(11474, "ERZ015369");
        erzDict.put(50, "ERZ017122");
        erzDict.put(232, "ERZ019958");
        erzDict.put(34705, "ERZ108740");
        erzDict.put(34890, "ERZX00065");
        erzDict.put(11471, "ERZ015361");
        erzDict.put(8595, "ERZX00047");
        erzDict.put(34892, "ERZX00053");
        erzDict.put(5537, "ERZ094205");
        erzDict.put(11495, "ERZ015348");
        erzDict.put(11531, "ERZ015366");
        erzDict.put(212, "ERZ019941");
        erzDict.put(5542, "ERZ094204");
        erzDict.put(5574, "ERZ094195");
        erzDict.put(11527, "ERZ015367");
        erzDict.put(34926, "ERZX00062");
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
