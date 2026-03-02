package uk.ac.ebi.eva.server.ws;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.lib.metadata.eva.SubmissionStatsEvaproDBAdaptor;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubmissionStatsWSServerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private SubmissionStatsEvaproDBAdaptor submissionStatsAdaptor;

    @Before
    public void setup() {
        Map<String, Long> countByMonth = new TreeMap<>();
        countByMonth.put("202301", 5L);
        countByMonth.put("202302", 3L);
        given(submissionStatsAdaptor.getCountByMonth()).willReturn(countByMonth);

        Map<String, Long> bytesByMonth = new TreeMap<>();
        bytesByMonth.put("202301", 1024L);
        bytesByMonth.put("202302", 2048L);
        given(submissionStatsAdaptor.getBytesByMonth()).willReturn(bytesByMonth);
    }

    @Test
    public void testGetSubmissionsCountPerMonth() {
        ResponseEntity<String> response = restTemplate.getForEntity("/v1/stats/submissions/count", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().toString().contains("text/plain"));

        String body = response.getBody();
        assertTrue(body.startsWith("Month\tEntries\n"));
        assertTrue(body.contains("202301\t5\n"));
        assertTrue(body.contains("202302\t3\n"));
    }

    @Test
    public void testGetSubmissionsBytesPerMonth() {
        ResponseEntity<String> response = restTemplate.getForEntity("/v1/stats/submissions/bytes", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().toString().contains("text/plain"));

        String body = response.getBody();
        assertTrue(body.startsWith("Month\tBytes\n"));
        assertTrue(body.contains("202301\t1024\n"));
        assertTrue(body.contains("202302\t2048\n"));
    }

}
