/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2015 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.server.ws;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.VariantStudy;
import org.opencb.biodata.models.variant.stats.VariantGlobalStats;
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

import uk.ac.ebi.eva.commons.models.data.VariantSourceEntity;
import uk.ac.ebi.eva.commons.mongodb.repositories.VariantSourceEntityRepository;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FilesWSServerTest {

    private static final String FILE_ID = "test_fid";

    private static final int VARIANTS_COUNT = 10;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantSourceEntityRepository variantSourceEntityRepository;

    @Before
    public void setup() throws Exception {
        Map<String, Object> metadata = new TreeMap<>();
        Map<String, Integer> samples = new TreeMap<>();
        VariantGlobalStats variantGlobalStats = new VariantGlobalStats();
        variantGlobalStats.setVariantsCount(VARIANTS_COUNT);

        VariantSourceEntity variantSourceEntity = new VariantSourceEntity(FILE_ID, "", "", "",
                VariantStudy.StudyType.COLLECTION, VariantSource.Aggregation.NONE, samples, metadata,
                variantGlobalStats);
        List<VariantSourceEntity> variantSourceEntities = Collections.singletonList(variantSourceEntity);

        given(variantSourceEntityRepository.findAll()).willReturn(variantSourceEntities);
    }
    
    @Test
    public void testGetFiles() throws URISyntaxException {
        String url = "/v1/files/all?species=hsapiens_grch37";
        ResponseEntity<QueryResponse<QueryResult<VariantSourceEntity>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantSourceEntity>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<VariantSourceEntity>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantSourceEntity> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(1, results.size());

        for (VariantSourceEntity variantSourceEntity : results) {
            assertEquals(FILE_ID, variantSourceEntity.getFileId());
            assertNotNull(variantSourceEntity.getFileName());
            assertNotNull(variantSourceEntity.getStudyId());
            assertNotNull(variantSourceEntity.getStudyName());

            VariantGlobalStats stats = variantSourceEntity.getStats();
            assertNotNull(stats);
            assertEquals(VARIANTS_COUNT, stats.getVariantsCount());
        }
    }

}
