package uk.ac.ebi.variation.eva.server.ws.ga4gh;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import uk.ac.ebi.variation.eva.server.ws.EvaWSServerTest;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by parce on 16/12/15.
 */
public class GA4GHVariantWSServerTest extends EvaWSServerTest {

    @Test
    public void testGetVariantsByRegion() throws Exception {
        Response response = given()
                .param("species", "hsapiens")
                .param("referenceName", "18")
                .param("start", "3000024")
                .param("end", "3000024")
                .get(new URI("/v1/ga4gh/variants/search"));
        List queryResponse = JsonPath.from(response.asString()).getList("variants");
        assertTrue(queryResponse.size() >= 1);

        for(Object variant : queryResponse) {
            String fileId = (String)((Map)variant).get("variantSetId");
            assertTrue("variant file id bad formatted: " + fileId, fileId.startsWith("ERZ"));
        }
    }

//    @Test
//    public void testGetVariantsByRegionFilteringByFile() throws Exception {
//        Response response = given()
//                .param("species", "hsapiens")
//                .param("referenceName", "18")
//                .param("start", "3000024")
//                .param("end", "3000024")
//                .param("variantSetIds", "ERZ038105")
//                .get(new URI("/v1/ga4gh/variants/search"));
//        List queryResponse = JsonPath.from(response.asString()).getList("variants");
//        assertTrue(queryResponse.size() == 2);
//
//        for(Object variant : queryResponse) {
//            String fileId = (String)((Map)variant).get("variantSetId");
//            assertTrue("variant file id bad formatted: " + fileId, fileId.startsWith("ERZ"));
//        }
//    }
}