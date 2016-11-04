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

/**
 * @author Tom Smith
 */
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
        assertTrue(result.size() == 1);

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

//    @Test
//    public void testGetVariantByIdDoesntExist() throws URISyntaxException {
//        String testId = "notarealid";
//        Response response = given().param("species", "mmusculus_grcm38").get(new URI("/v1/variants/" + testId + "/info"));
//        response.then().statusCode(200);
//    }

    @Test
    public void testGetVariantByRegionDoesntExist() throws URISyntaxException {
        String testRegion = "20:71821:C:G";
        Response response = given().param("species", "mmusculus_grcm38").get(new URI("/v1/variants/" + testRegion + "/info"));
        response.then().statusCode(200);

        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(1, queryResponse.size());

        List<Map> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
        assertTrue(result.size() == 0);
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
