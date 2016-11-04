package uk.ac.ebi.eva.server.ws;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencb.biodata.models.variant.Variant;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
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

    @Test
    public void testGetVariantById() throws URISyntaxException {
        Response response = given().param("species", "mmusculus_grcm38").get(new URI("/v1/variants/20_71822_C_G"));  // TODO add variant id to end of this uri
        response.then().statusCode(200);

        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(1, queryResponse.size());

        List<Map> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
        assertTrue(result.size() == 1);

        for (Map m : result) {
            String missingField = String.format("%s required field missing", m.get("name"));

            assertTrue(missingField, m.containsKey("id"));
            assertTrue(missingField, m.containsKey("name"));
            assertTrue(missingField, m.containsKey("feature"));
            assertTrue(missingField, m.containsKey("chromosome"));
            assertTrue(missingField, m.containsKey("start"));
            assertTrue(missingField, m.containsKey("end"));
        }

    }

}
