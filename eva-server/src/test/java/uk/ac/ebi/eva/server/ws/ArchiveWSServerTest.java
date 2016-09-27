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
package uk.ac.ebi.eva.server.ws;

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


public class ArchiveWSServerTest {
    
    @BeforeClass
    public static void startJetty() throws Exception {
        RestAssured.port = 8080;
        RestAssured.baseURI = "http://localhost:8080/eva/webservices/rest";
    }
    
    @Test
    public void testCountFiles() throws URISyntaxException {
        Response response = get(new URI("/v1/meta/files/count"));
        response.then().statusCode(200);
        
        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(1, queryResponse.size());
        
        List<Integer> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
        assertEquals(1, result.size());
        assertTrue(result.get(0) >= 0);
    }
    
    @Test
    public void testCountSpecies() throws URISyntaxException {
        Response response = get(new URI("/v1/meta/species/count"));
        response.then().statusCode(200);
        
        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(1, queryResponse.size());
        
        List<Integer> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
        assertEquals(1, result.size());
        assertTrue(result.get(0) >= 0);
    }

    @Test
    public void testGetSpecies() throws URISyntaxException {
        Response response = get(new URI("/v1/meta/species/list"));
        response.then().statusCode(200);
        
        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(1, queryResponse.size());
        
        List<Map> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
        assertTrue(result.size() >= 1);
        
        for (Map m : result) {
            String missingField = String.format("%s required field missing", m.get("assemblyName"));
            
            if (m.containsKey("assemblyChain")) { // Accessioned assembly
                assertTrue(missingField, m.containsKey("assemblyAccession"));
                assertTrue(missingField, m.containsKey("assemblyChain"));
                assertTrue(missingField, m.containsKey("assemblyCode"));
                assertTrue(missingField, m.containsKey("assemblyName"));
                assertTrue(missingField, m.containsKey("assemblyVersion"));
            } else { // Non-accessioned assembly
                assertTrue(missingField, m.containsKey("assemblyCode"));
                assertTrue(missingField, m.containsKey("assemblyName"));
            }
            
            assertTrue(missingField, m.containsKey("taxonomyId"));
            assertTrue(missingField, m.containsKey("taxonomyCode"));
//            assertTrue(missingField, m.containsKey("taxonomyCommonName")); // This is not really mandatory
            assertTrue(missingField, m.containsKey("taxonomyEvaName"));
            assertTrue(missingField, m.containsKey("taxonomyScientificName"));
        }
    }
    
    @Test
    public void testGetLoadedSpecies() throws URISyntaxException {
        Response response = get(new URI("/v1/meta/species/list"));
        response.then().statusCode(200);
        
        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(1, queryResponse.size());
        
        List<Map> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
        assertTrue(result.size() >= 1);
        
        for (Map m : result) {
            String missingField = String.format("%s required field missing", m.get("assemblyName"));
            
            if (m.containsKey("assemblyChain")) { // Accessioned assembly
                assertTrue(missingField, m.containsKey("assemblyAccession"));
                assertTrue(missingField, m.containsKey("assemblyChain"));
                assertTrue(missingField, m.containsKey("assemblyCode"));
                assertTrue(missingField, m.containsKey("assemblyName"));
                assertTrue(missingField, m.containsKey("assemblyVersion"));
            } else { // Non-accessioned assembly
                assertTrue(missingField, m.containsKey("assemblyCode"));
                assertTrue(missingField, m.containsKey("assemblyName"));
            }
            
            assertTrue(missingField, m.containsKey("taxonomyId"));
            assertTrue(missingField, m.containsKey("taxonomyCode"));
//            assertTrue(missingField, m.containsKey("taxonomyCommonName")); // This is not really mandatory
            assertTrue(missingField, m.containsKey("taxonomyEvaName"));
            assertTrue(missingField, m.containsKey("taxonomyScientificName"));
        }
    }

    @Test
    public void testEqualOrLessLoadedSpeciesThanTotal() throws URISyntaxException {
        String allResponse = get(new URI("/v1/meta/species/list")).asString();
        List<Map> allResult = JsonPath.from(allResponse).getJsonObject("response[0].result");
        assertTrue(allResult.size() >= 1);
        
        String loadedResponse = given().params("loaded", true).get(new URI("/v1/meta/species/list")).asString();
        List<Map> loadedResult = JsonPath.from(loadedResponse).getJsonObject("response[0].result");
        assertTrue(loadedResult.size() >= 1);
        
        assertTrue(loadedResult.size() <= allResult.size());
    }
    
    @Test
    public void testCountStudies() throws URISyntaxException {
        Response response = get(new URI("/v1/meta/studies/count"));
        response.then().statusCode(200);
        
        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(1, queryResponse.size());
        
        List<Integer> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
        assertEquals(1, result.size());
        assertTrue(result.get(0) >= 0);
    }

    @Test
    public void testGetBrowsableStudiesNoSpecies() throws URISyntaxException {
        Response response = get(new URI("/v1/meta/studies/list"));
        response.then().statusCode(400);
    }

    @Test
    public void testGetBrowsableStudiesBySpecies() throws URISyntaxException {
        Response response = given().param("species", "hsapiens_grch37").get(new URI("/v1/meta/studies/list"));
        response.then().statusCode(200);
        
        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(1, queryResponse.size());
        
        List<Map> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
        assertTrue(result.size() >= 1);
    }

    @Test
    public void testGetStudies() throws URISyntaxException {
        Response response = get(new URI("/v1/meta/studies/all"));
        response.then().statusCode(200);
        
        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(1, queryResponse.size());
        
        List<Map> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
        assertTrue(result.size() >= 1);
        
        for (Map m : result) {
            String missingField = String.format("%s required field missing", m.get("name"));
            
            assertTrue(missingField, m.containsKey("name"));
            assertTrue(missingField, m.containsKey("id"));
            assertTrue(missingField, m.containsKey("description"));
            
            assertTrue(missingField, m.containsKey("taxonomyId"));
            List<Integer> taxonomyIds = JsonPath.from(response.asString()).getJsonObject("response[0].result.taxonomyId");
            assertFalse(taxonomyIds.isEmpty());
            
            assertTrue(missingField, m.containsKey("speciesCommonName"));
            assertTrue(missingField, m.containsKey("speciesScientificName"));
            assertTrue(missingField, m.containsKey("sourceType"));
            assertTrue(missingField, m.containsKey("center"));
            assertTrue(missingField, m.containsKey("material"));
            assertTrue(missingField, m.containsKey("scope"));
            assertTrue(missingField, m.containsKey("experimentType"));
            assertTrue(missingField, m.containsKey("experimentTypeAbbreviation"));
            assertTrue(missingField, m.containsKey("assembly"));
            assertTrue(missingField, m.containsKey("platform"));
            assertTrue(missingField, m.containsKey("url"));
            assertTrue(missingField, m.containsKey("publications"));
            assertTrue(missingField, m.containsKey("numVariants"));
            assertTrue(missingField, m.containsKey("numSamples"));
        }
    }

    @Test
    public void testGetStudiesStats() throws URISyntaxException {
        Response response = get(new URI("/v1/meta/studies/stats"));
        response.then().statusCode(200);
        
        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(1, queryResponse.size());
        
        Map<String, Integer> species = JsonPath.from(response.asString()).getJsonObject("response[0].result[0].species");
        assertTrue(species.size() >= 1);
        
        // instanceof are necessary to make it really evaluate the types
        for (Map.Entry<String, Integer> m : species.entrySet()) {
            assertTrue(m.getKey() instanceof String);
            assertTrue(m.getValue() instanceof Integer);
        }
        
        Map<String, Integer> types = JsonPath.from(response.asString()).getJsonObject("response[0].result[0].type");
        assertTrue(types.size() >= 1);
        
        // instanceof is necessary to make it really evaluate the types
        for (Map.Entry<String, Integer> m : types.entrySet()) {
            assertTrue(m.getKey() instanceof String);
            assertTrue(m.getValue() instanceof Integer);
        }
    }
    
}
