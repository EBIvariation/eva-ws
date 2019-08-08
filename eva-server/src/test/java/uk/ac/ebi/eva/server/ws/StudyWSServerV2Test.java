package uk.ac.ebi.eva.server.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
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
import uk.ac.ebi.eva.commons.mongodb.entities.projections.VariantStudySummary;
import uk.ac.ebi.eva.commons.mongodb.services.VariantStudySummaryService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudyWSServerV2Test {

    private static final long PAGE_SIZE = 2;

    private static final long PAGE_NUMBER = 0;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantStudySummaryService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        List<VariantStudySummary> studies = new ArrayList<>();
        VariantStudySummary study1 = new VariantStudySummary();
        VariantStudySummary study2 = new VariantStudySummary();

        study1.setFilesCount(1);
        study1.setStudyId("studyId1");
        study1.setStudyName("studyName1");
        studies.add(study1);

        study2.setFilesCount(2);
        study2.setStudyId("studyId2");
        study2.setStudyName("studyName2");
        studies.add(study2);

        given(service.findAll(eq(PAGE_NUMBER), eq(PAGE_SIZE))).willReturn(studies);
        given(service.countAll()).willReturn(2);
    }

    @Test
    public void testGetStudies() {
        String url = "/v2/studies?species=mmusculus&assembly=grcm38&pageNumber=0&pageSize=2";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Configuration configuration = Configuration.defaultConfiguration()
                .jsonProvider(new JacksonJsonProvider())
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .addOptions(Option.SUPPRESS_EXCEPTIONS);

        List<VariantStudySummary> variantList = JsonPath.using(configuration).parse(response.getBody())
                .read("$['_embedded']['variantStudySummaryList']", new TypeRef<List<VariantStudySummary>>() {
                });
        System.out.println(response.getBody());
        assertEquals("studyId1", variantList.get(0).getStudyId());
        assertEquals("studyName1", variantList.get(0).getStudyName());
        assertEquals("studyId2", variantList.get(1).getStudyId());
        assertEquals("studyName2", variantList.get(1).getStudyName());
    }
}
