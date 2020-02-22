/*
 * Copyright 2019 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.eva.commons.core.models.DefaultLocusRangeMetadata;
import uk.ac.ebi.eva.commons.mongodb.services.DefaultLocusRangeMetadataService;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefaultLocusRangeMetadataWSServerTest {

    private List<DefaultLocusRangeMetadata> defaultLocusRangeMetadataList;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private DefaultLocusRangeMetadataService service;

    @Before
    public void setUp() throws Exception {
        defaultLocusRangeMetadataList = Arrays.asList(new DefaultLocusRangeMetadata("1", 1, 1000000));

        given(service.findAllByOrderByChromosomeAscStartAscEndAsc()).willReturn(defaultLocusRangeMetadataList);
    }

    @Test
    public void testDefaultLocusRangeMetadata() {
        String url = "/v1/default-locus-range?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<DefaultLocusRangeMetadata>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<DefaultLocusRangeMetadata>>>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<DefaultLocusRangeMetadata>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<DefaultLocusRangeMetadata> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(defaultLocusRangeMetadataList.size(), results.size());

        for (DefaultLocusRangeMetadata result : results) {
            assertTrue(defaultLocusRangeMetadataList.contains(result));
        }
    }

}