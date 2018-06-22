package uk.ac.ebi.dgva.server.ws;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.core.models.StudyType;
import uk.ac.ebi.eva.lib.metadata.dgva.StudyDgvaDBAdaptor;
import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudyWSServerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private StudyDgvaDBAdaptor studyDgvaDBAdaptor;

    private static final String EXISTING_STUDY = "svS1";

    private static final String NOT_EXISTING_STUDY = "svS2";

    private VariantStudy svStudy1;

    @Before
    public void setUp() throws Exception {
        svStudy1 = new VariantStudy("Human SV Test study 1", EXISTING_STUDY, null,
                                    "SV study 1 description", new int[]{9606}, "Human", "Homo Sapiens",
                                    "Germline", "EBI", "DNA", "multi-isolate", StudyType.CASE_CONTROL,
                                    "Exome Sequencing", "ES", "GRCh37", "GCA_000001405.3", "Illumina",
                                    new URI("http://www.s1.org"), new String[]{"10"}, 1000, 10, false);
        given(studyDgvaDBAdaptor.getStudyById(eq(EXISTING_STUDY), anyObject()))
                .willReturn(encapsulateInQueryResult(svStudy1));
        given(studyDgvaDBAdaptor.getStudyById(eq(NOT_EXISTING_STUDY), anyObject()))
                .willReturn(encapsulateInQueryResult());
    }

    private <T> QueryResult<T> encapsulateInQueryResult(T... results) {
        return new QueryResult<>(null, 0, results.length, results.length, null, null, Arrays.asList(results));
    }

    @Test
    public void getStudySummary() {
        QueryResponse<QueryResult<VariantStudy>> queryResponse =
                callEndpoint("/v1/studies/" + EXISTING_STUDY + "/summary");
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantStudy> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(1, results.size());
        assertEquals(svStudy1.getId(), results.get(0).getId());
        assertEquals(svStudy1.getName(), results.get(0).getName());
    }

    @Test
    public void getNotExistentStudySummary() {
        QueryResponse<QueryResult<VariantStudy>> queryResponse =
                callEndpoint("/v1/studies/" + NOT_EXISTING_STUDY + "/summary");
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantStudy> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(0, results.size());
    }

    private QueryResponse<QueryResult<VariantStudy>> callEndpoint(String url) {
        ResponseEntity<QueryResponse<QueryResult<VariantStudy>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantStudy>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());

        return response.getBody();
    }
}