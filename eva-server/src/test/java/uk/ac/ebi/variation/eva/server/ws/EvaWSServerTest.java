package uk.ac.ebi.variation.eva.server.ws;

import com.jayway.restassured.RestAssured;
import org.junit.BeforeClass;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by parce on 16/12/15.
 */
public class EvaWSServerTest {

    @BeforeClass
    public static void startJetty() throws Exception {
        RestAssured.port = 8080;
        RestAssured.baseURI = "http://localhost:8080/eva/webservices/rest";
    }

    protected void checkProjectAndStudyIds(List<Map<String, Map>> results) {
        for (Map<String, Map> result : results) {
            Collection<Map> sourceEntries = result.get("sourceEntries").values();
            for (Map sourceEntry : sourceEntries) {
                String fileId = (String) sourceEntry.get("fileId");
                assertTrue("fileId " + fileId + " seems to be not valid", fileId.startsWith("ERZ"));
                String studyId = (String) sourceEntry.get("studyId");
                assertTrue("studyId " + studyId + " seems to be not valid", studyId.startsWith("PRJ"));
            }
        }
    }
}
