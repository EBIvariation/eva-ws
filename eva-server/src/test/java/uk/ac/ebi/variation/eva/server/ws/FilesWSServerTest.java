/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2015 EMBL - European Bioinformatics Institute
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

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.jayway.restassured.RestAssured.*;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;


public class FilesWSServerTest {
    
    @BeforeClass
    public static void startJetty() throws Exception {
        RestAssured.port = 8080;
        RestAssured.baseURI = "http://localhost:8080/eva/webservices/rest";
    }
    
    @Test
    public void testGetFiles() throws URISyntaxException {
        Response response = given().param("species", "hsapiens_grch37").get(new URI("/v1/files/all"));
        response.then().statusCode(200);
        
        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(1, queryResponse.size());
        
        List<Map> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
        assertTrue(result.size() >= 1);
        
        for (int i = 0; i < result.size() ; i++) {
            Map m = result.get(i);
            String missingField = String.format("%s required field missing", m.get("name"));
            
            assertTrue(missingField, m.containsKey("fileName"));
            assertTrue(missingField, m.containsKey("fileId"));
            assertTrue(missingField, m.containsKey("studyName"));
            assertTrue(missingField, m.containsKey("studyId"));
            assertTrue(missingField, m.containsKey("aggregation"));
            assertTrue(missingField, m.containsKey("type"));
            assertTrue(missingField, m.containsKey("metadata"));
            assertTrue(missingField, m.containsKey("samplesPosition"));
            
            if (m.containsKey("stats")) {
                Map<String, ?> stats = JsonPath.from(response.asString()).getJsonObject("response[0].result[" + i + "].stats");
                assertTrue(missingField, stats.containsKey("variantsCount"));
                assertTrue(missingField, stats.containsKey("samplesCount"));
                assertTrue(missingField, stats.containsKey("snpsCount"));
                assertTrue(missingField, stats.containsKey("indelsCount"));
                assertTrue(missingField, stats.containsKey("structuralCount"));
                assertTrue(missingField, stats.containsKey("passCount"));
                assertTrue(missingField, stats.containsKey("transitionsCount"));
                assertTrue(missingField, stats.containsKey("transversionsCount"));
                assertTrue(missingField, stats.containsKey("accumulatedQuality"));
                assertTrue(missingField, stats.containsKey("meanQuality"));
            }
        }
    }

}
