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
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IdentifierWSServerV2Test {

    private static final VariantWithSamplesAndAnnotation VARIANT = new VariantWithSamplesAndAnnotation("1", 1000, 1005,
            "A", "T", "rs1");

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantWithSamplesAndAnnotationsService variantEntityRepository;

    @Before
    public void setUp() throws Exception {
        List<VariantWithSamplesAndAnnotation> variantEntities = Collections.singletonList(VARIANT);
        given(variantEntityRepository.findByIdsAndComplexFilters(Arrays.asList("ss481155011"), null, null, null,
                null)).willReturn(Arrays.asList(VARIANT));
    }

    @Test
    public void testForExisting() {
        String url = "/v2/identifiers/ss481155011?species=hsapiens&assembly=grch37";
        ResponseEntity<List<Variant>> response = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Variant>>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Variant> variantList = response.getBody();
        assertTrue(variantList.size() > 0);
        assertEquals("1", variantList.get(0).getChromosome());
        assertEquals("T", variantList.get(0).getAlternate());
        assertEquals("A", variantList.get(0).getReference());
    }

    @Test
    public void testForNonExisting() {
        String url = "/v2/identifiers/abcd?species=hsapiens&assembly=grch37";
        ResponseEntity<List<Variant>> response = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Variant>>() {
                });
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().size() == 0);
    }

}
