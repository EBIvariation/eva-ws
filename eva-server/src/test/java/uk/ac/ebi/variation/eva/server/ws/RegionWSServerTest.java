package uk.ac.ebi.variation.eva.server.ws;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.junit.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by parce on 16/12/15.
 */
public class RegionWSServerTest extends EvaWSServerTest {

    @Test
    public void getVariantsByRegion() throws Exception {
        Response response = given().param("species", "hsapiens").param("histogram", "false").get(new URI("/v1/segments/" +
                "18:3000020-3000025,18:3000030-3000035/variants"));
        List queryResponse = JsonPath.from(response.asString()).getList("response");
        assertEquals(2, queryResponse.size());

        List<Map<String, Map>> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
        assertTrue(result.size() >= 1);
        checkProjectAndStudyIds(result);

        result = JsonPath.from(response.asString()).getJsonObject("response[1].result");
        assertTrue(result.size() >= 1);
        checkProjectAndStudyIds(result);
    }
}