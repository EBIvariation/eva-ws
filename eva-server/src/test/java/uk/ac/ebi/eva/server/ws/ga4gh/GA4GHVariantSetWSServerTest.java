package uk.ac.ebi.eva.server.ws.ga4gh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.eva.commons.core.models.Aggregation;
import uk.ac.ebi.eva.commons.core.models.StudyType;
import uk.ac.ebi.eva.commons.core.models.VariantSource;
import uk.ac.ebi.eva.commons.mongodb.services.VariantSourceService;
import uk.ac.ebi.eva.lib.models.ga4gh.GASearchVariantSetsResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GA4GHVariantSetWSServerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantSourceService service;


    @BeforeEach
    public void setUp() throws Exception {
        Map<String, Integer> samplesPosition = new HashMap<>();
        samplesPosition.put("sample1", 123);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("header", "myHeader");

        VariantSource variantSourceEntity = new VariantSource("fileId", "fileName", "studyId", "studyName",
                StudyType.CASE, Aggregation.NONE, null, samplesPosition, metadata, null);


        List<VariantSource> variantSourceEntities = Collections.singletonList(variantSourceEntity);

        given(service.findByStudyIdIn(eq(Collections.singletonList("studyId")), any()))
                .willReturn(variantSourceEntities);

        given(service.countByStudyIdIn(eq(Collections.singletonList("studyId"))))
                .willReturn(Long.valueOf(variantSourceEntities.size()));
    }

    @Test
    public void testGetVariantSetsExisting() {
        GASearchVariantSetsResponse response = testGetVariantSetsHelper(Collections.singletonList("studyId"));
        assertEquals(1, response.getVariantSets().size());
    }

    @Test
    public void testGetVariantSetsNotExisting() {
        GASearchVariantSetsResponse response = testGetVariantSetsHelper(Collections.singletonList("otherStudyId"));
        assertEquals(0, response.getVariantSets().size());
    }

    private GASearchVariantSetsResponse testGetVariantSetsHelper(List<String> datasetIds) {
        String url = String.format("/v1/ga4gh/variantsets/search?datasetIds=%s", String.join(",", datasetIds));

        ResponseEntity<GASearchVariantSetsResponse> response = restTemplate.getForEntity(
                url, GASearchVariantSetsResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        return response.getBody();
    }

}