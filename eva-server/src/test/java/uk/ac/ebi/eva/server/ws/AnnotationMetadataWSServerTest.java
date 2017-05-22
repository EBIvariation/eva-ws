/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
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

import uk.ac.ebi.eva.commons.models.metadata.AnnotationMetadata;
import uk.ac.ebi.eva.lib.repository.AnnotationMetadataRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AnnotationMetadataWSServerTest {

    private List<AnnotationMetadata> annotationMetadataList;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private AnnotationMetadataRepository annotationMetadataRepository;

    @Before
    public void setUp() throws Exception {
        annotationMetadataList = Arrays.asList(new AnnotationMetadata("75", "75"),
                                               new AnnotationMetadata("74", "74"));

        given(annotationMetadataRepository.findAllByOrderByCacheVersionDescVepVersionDesc())
                .willReturn(annotationMetadataList);
    }

    @Test
    public void testFetAnnotationMetadata() {
        String url = "/v1/annotation?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<AnnotationMetadata>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<AnnotationMetadata>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<AnnotationMetadata>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<AnnotationMetadata> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(annotationMetadataList.size(), results.size());

        for (AnnotationMetadata result : results) {
            assertTrue(annotationMetadataList.contains(result));
        }
    }

}