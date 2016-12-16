package uk.ac.ebi.eva.server.ws;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.lib.metadata.ArchiveEvaproDBAdaptor;
import uk.ac.ebi.eva.lib.repository.FileRepository;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArchiveWSServerSpringTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ArchiveEvaproDBAdaptor archiveEvaproDBAdaptor;

    @Before
    public void setup() {
        given(this.archiveEvaproDBAdaptor.countFiles()).willReturn(new QueryResult<>(null, 0, 1, 1, null, null,
                                                                                     Collections.singletonList(5L)));
        given(this.archiveEvaproDBAdaptor.countSpecies()).willReturn(new QueryResult<>(null, 0, 1, 1, null, null,
                                                                                     Collections.singletonList(4L)));
    }

    @Test
    public void testCountFiles() throws URISyntaxException {
        ResponseEntity<QueryResponse> resp1 = this.restTemplate.getForEntity("/v1/meta/species/count", QueryResponse.class);
        assertEquals(resp1.getStatusCode(), HttpStatus.OK );


        ParameterizedTypeReference<QueryResponse<QueryResult<Long>>> typeReference = new ParameterizedTypeReference<QueryResponse<QueryResult<Long>>>() {};
//        ResponseEntity<QueryResponse<Long>> response = this.restTemplate.getForEntity("/v1/meta/files/count", ParameterizedTypeReference<QueryResponse<Long>>);
        ResponseEntity<QueryResponse<QueryResult<Long>>> response = this.restTemplate.exchange("/v1/meta/files/count", HttpMethod.GET, null, typeReference);
        assertEquals(response.getStatusCode(), HttpStatus.OK );
        List<QueryResult<Long>> queryResponse = response.getBody().getResponse();

        List results = (List)((Map)queryResponse.get(0)).get("result");
        assertEquals(1, results.size());
        assertEquals(new Long(5), results.get(0));


//        assertEquals(5, ((List)((Map)queryResponse.get(0)).get("result")).get(0));



//        List<Integer> result = JsonPath.from(response.asString()).getJsonObject("response[0].result");
//        assertEquals(1, result.size());
//        assertTrue(result.get(0) >= 0);
    }
}
