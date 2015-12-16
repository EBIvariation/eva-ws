package uk.ac.ebi.variation.eva.server.ws;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.junit.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by parce on 15/12/15.
 */
public class GeneWSServerTest extends EvaWSServerTest {

    @Test
    public void testGetVariantsByGeneFileIds() throws Exception {
        Response response = given().param("species", "hsapiens").param("limit", "2").get(new URI("/v1/genes/BRCA2/variants"));
        response.then().statusCode(200);

        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(1, queryResponse.size());

        List<Map<String, Map>> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
        assertTrue(result.size() == 2);
        checkProjectAndStudyIds(result);
    }
}