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

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VariantWSServerTest {

    @BeforeClass
    public static void startJetty() throws Exception {
        RestAssured.port = 8080;
        RestAssured.baseURI = "http://localhost:8080/eva/webservices/rest";
    }

    private void testGetVariantByIdRegionHelper(String testString) throws URISyntaxException {
        Response response = given().param("species", "mmusculus_grcm38").get(new URI("/v1/variants/" + testString + "/info"));
        response.then().statusCode(200);

        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(1, queryResponse.size());

        List<Map> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");

        Map m = result.get(0);

        String missingField = String.format("%s required field missing", m.get("id"));

        assertTrue(missingField, m.containsKey("id"));
        assertTrue(missingField, m.containsKey("type"));
        assertTrue(missingField, m.containsKey("chromosome"));
        assertTrue(missingField, m.containsKey("start"));
        assertTrue(missingField, m.containsKey("end"));
        assertTrue(missingField, m.containsKey("length"));
        assertTrue(missingField, m.containsKey("reference"));
        assertTrue(missingField, m.containsKey("alternate"));
    }

    @Test
    public void testGetVariantById() throws URISyntaxException {
        testGetVariantByIdRegionHelper("rs567000874");
    }

    @Test
    public void testGetVariantByRegion() throws URISyntaxException {
        testGetVariantByIdRegionHelper("20:71822:C:G");
    }

    ///

    private void testGetVariantByIdRegionDoesntExistHelper(String testString) throws URISyntaxException {
        Response response = given().param("species", "mmusculus_grcm38").get(new URI("/v1/variants/" + testString + "/info"));
        response.then().statusCode(200);

        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(1, queryResponse.size());

        List<Map> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
        assertTrue(result.size() == 0);
    }

    @Test
    public void testGetVariantByIdDoesntExist() throws URISyntaxException {
        testGetVariantByIdRegionDoesntExistHelper("notARealId");
    }

    @Test
    public void testGetVariantByRegionDoesntExist() throws URISyntaxException {
        testGetVariantByIdRegionDoesntExistHelper("20:71821:C:G");
    }

    ///

    private Boolean testCheckVariantExistsHelper(String testRegion) throws URISyntaxException {
        Response response = given().param("species", "mmusculus_grcm38").get(new URI("/v1/variants/" + testRegion + "/exists"));
        response.then().statusCode(200);

        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(1, queryResponse.size());

        List<Boolean> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
        assertTrue(result.size() == 1);

        return result.get(0);
    }

    @Test
    public void testCheckVariantExistsDoesExist() throws URISyntaxException {
        assertTrue(testCheckVariantExistsHelper("20:71822:C:G"));
    }

    @Test
    public void testCheckVariantExistsDoesntExist() throws URISyntaxException {
        assertFalse(testCheckVariantExistsHelper("20:71821:C:G"));
    }



}
