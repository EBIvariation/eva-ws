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
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        List<VariantWithSamplesAndAnnotation> variantEntities = Collections.singletonList(VARIANT);
        given(variantEntityRepository.findByIdsAndComplexFilters(Arrays.asList("ss481155011"), null, null, null,
                null)).willReturn(Arrays.asList(VARIANT));
    }

    @Test
    public void testForExisting() {
        String url = "/v2/identifiers/ss481155011?species=hsapiens&assembly=grch37";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Configuration configuration = Configuration.defaultConfiguration()
                .jsonProvider(new JacksonJsonProvider())
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .addOptions(Option.SUPPRESS_EXCEPTIONS);
        List<Variant> variantList = JsonPath.using(configuration).parse(response.getBody())
                .read("$['_embedded']['variantList']", new TypeRef<List<Variant>>() {
                });
        assertFalse(JsonPath.using(configuration).parse(response.getBody()).read
                ("$['_embedded']['variantList'][0]['_links']['sources']['href']", new TypeRef<String>() {
                }).isEmpty());
        assertFalse(JsonPath.using(configuration).parse(response.getBody()).read
                ("$['_embedded']['variantList'][0]['_links']['annotation']['href']", new TypeRef<String>() {
                }).isEmpty());

        assertTrue(variantList.size() > 0);
        Variant variant = variantList.get(0);
        assertEquals("1", variant.getChromosome());
        assertEquals("T", variant.getAlternate());
        assertEquals("A", variant.getReference());
        assertEquals(1000L, variant.getStart());
        assertEquals(1005L, variant.getEnd());
    }

    @Test
    public void testForNonExisting() {
        String url = "/v2/identifiers/abcd?species=hsapiens&assembly=grch37";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

}
