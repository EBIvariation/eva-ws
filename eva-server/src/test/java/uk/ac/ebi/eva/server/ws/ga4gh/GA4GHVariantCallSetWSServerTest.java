/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.server.ws.ga4gh;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencb.biodata.ga4gh.GASearchCallSetsResponse;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.VariantStudy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.models.data.VariantSourceEntity;
import uk.ac.ebi.eva.lib.repository.VariantSourceEntityRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GA4GHVariantCallSetWSServerTest {

    private static VariantSourceEntity VARIANT_SOURCE_ENTITY =
            new VariantSourceEntity("fileId", "fileName", "studyId", "studyName", VariantStudy.StudyType.CASE,
                                    VariantSource.Aggregation.NONE, null, null, null);

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantSourceEntityRepository variantSourceEntityRepository;

    @Before
    public void setUp() throws Exception {
        Map<String, Integer> samplesPosition = new HashMap<>();
        samplesPosition.put("sample1", 123);
        VARIANT_SOURCE_ENTITY.setSamplesPosition(samplesPosition);

        List<VariantSourceEntity> variantSourceEntities = Collections.singletonList(VARIANT_SOURCE_ENTITY);

        given(variantSourceEntityRepository.findByFileIdIn(eq(Collections.singletonList("fileId")), any()))
                .willReturn(variantSourceEntities);

        given(variantSourceEntityRepository.countByFileIdIn(eq(Collections.singletonList("fileId"))))
                .willReturn(Long.valueOf(variantSourceEntities.size()));
    }

    @Test
    public void testGetCallSetsExisting() {
        GASearchCallSetsResponse gaSearchCallSetsResponse = testGetCallSetsHelper(Collections.singletonList("fileId"));
        assertEquals(1, gaSearchCallSetsResponse.getCallSets().size());
    }

    @Test
    public void testGetCallSetsNotExisting() {
        GASearchCallSetsResponse response = testGetCallSetsHelper(Collections.singletonList("otherFileId"));
        assertEquals(0, response.getCallSets().size());
    }

    private GASearchCallSetsResponse testGetCallSetsHelper(List<String> variantSetIds) {
        String url = String.format("/v1/ga4gh/callsets/search?variantSetIds=%s", String.join(",", variantSetIds));

        ResponseEntity<GASearchCallSetsResponse> response = restTemplate.getForEntity(
                url, GASearchCallSetsResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        return response.getBody();
    }

}