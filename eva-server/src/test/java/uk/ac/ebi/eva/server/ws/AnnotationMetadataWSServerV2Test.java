/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.eva.commons.core.models.AnnotationMetadata;
import uk.ac.ebi.eva.commons.mongodb.services.AnnotationMetadataService;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AnnotationMetadataWSServerV2Test {

    private List<AnnotationMetadata> annotationMetadataList;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private AnnotationMetadataService service;

    @Before
    public void setUp() throws Exception {
        annotationMetadataList = Arrays.asList(new AnnotationMetadata("75", "75"),
                new AnnotationMetadata("74", "74"));

        given(service.findAllByOrderByCacheVersionDescVepVersionDesc()).willReturn(annotationMetadataList);
    }

    @Test
    public void testGetAnnotationMetadata() {
        String url = "/v2/annotation-versions?species=mmusculus&assembly=grcm38";
        ResponseEntity<List<AnnotationMetadata>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<AnnotationMetadata>>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<AnnotationMetadata> annotationMetadataList = response.getBody();
        assertEquals(2, annotationMetadataList.size());
    }
}
