/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2016 EMBL - European Bioinformatics Institute
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

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegionWSServerTest {

    @BeforeClass
    public static void startJetty() throws Exception {
        RestAssured.port = 8080;
        RestAssured.baseURI = "http://localhost:8080/eva/webservices/rest";
    }

    @Test
    public void testGetVariantsByRegion() throws URISyntaxException {
        testGetVariantsByRegionHelper("20:60000-62000", 40);
    }

    @Test
    public void testGetVariantsByRegions() throws URISyntaxException {
        testGetVariantsByRegionHelper("20:60000-61000,20:61500-62500", 17);
    }

    @Test
    public void testExcludeNested() throws URISyntaxException {
        Response response = given().param("species", "mmusculus_grcm38").param("exclude", "filesAttrs").get(new URI("/v1/segments/20:60000-62000/variants"));
        response.then().statusCode(200);
        List<Map> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
        for (Map m : result) {
            for (Map sourceEntry: (Collection<Map>) ((Map) m.get("sourceEntries")).values()) {
                System.out.println(sourceEntry);
                assertTrue(((Map) sourceEntry.get("attributes")).isEmpty());
            }
        }
    }

    private void testGetVariantsByRegionHelper(String testString, int expectedSize) throws URISyntaxException {
        Response response = given().param("species", "mmusculus_grcm38").get(new URI("/v1/segments/" + testString + "/variants"));
        response.then().statusCode(200);

        List queryResponse = JsonPath.from(response.asString()).getList("response");

        List<Map> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");

        for (Map m : result) {
            assertFalse(m.containsKey(""));

            String missingField = String.format("%s required field missing", m.get("name"));

            assertTrue(missingField, m.containsKey("id"));
            assertTrue(missingField, m.containsKey("chromosome"));
            assertTrue(missingField, m.containsKey("start"));
            assertTrue(missingField, m.containsKey("end"));
        }
    }

}
