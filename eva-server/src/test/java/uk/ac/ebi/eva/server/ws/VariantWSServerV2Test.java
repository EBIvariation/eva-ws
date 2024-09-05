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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.core.models.Annotation;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigNamingConvention;
import uk.ac.ebi.eva.commons.core.models.ws.VariantSourceEntryWithSampleNames;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.server.ws.contigalias.ContigAliasService;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VariantWSServerV2Test {

    private static final String CHROMOSOME = "existingChromosome";

    private static final String NON_EXISTING_CHROMOSOME = "notARealChromosome";

    private static final String MAIN_ID = "rs1";

    private static final VariantWithSamplesAndAnnotation VARIANT = new VariantWithSamplesAndAnnotation("1", 1000, 1005,
            "A", "T", MAIN_ID);

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantWithSamplesAndAnnotationsService service;

    @MockBean
    private ContigAliasService contigAliasService;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        VARIANT.addId("randomID");
        VARIANT.setAnnotation(new Annotation(CHROMOSOME, 0, 0, null, null, null, null));
        VARIANT.addSourceEntry(new VariantSourceEntryWithSampleNames("fid", "sid", null, null, null, null, null));
        List<VariantWithSamplesAndAnnotation> variantEntities = Collections.singletonList(VARIANT);

        given(service.findByChromosomeAndStartAndReferenceAndAlternate(eq(CHROMOSOME), anyLong(), any(), any(), any()))
                .willReturn(variantEntities);
        given(contigAliasService.translateContigFromInsdc(VARIANT.getChromosome(), null))
                .willReturn("");
    }

    @Test
    public void rootTestGetVariantsByVariantCoreString() throws URISyntaxException {
        String url = "/v2/variants/" + CHROMOSOME + ":71822:C:G?species=mmusculus&assembly=grcm38";
        ResponseEntity<VariantWithSamplesAndAnnotation> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<VariantWithSamplesAndAnnotation>() {
                });
        VariantWithSamplesAndAnnotation variantWithSamplesAndAnnotation = response.getBody();
        assertEquals(0, variantWithSamplesAndAnnotation.getSourceEntries().size());
        assertNull(variantWithSamplesAndAnnotation.getAnnotation());
        assertTrue(variantWithSamplesAndAnnotation.getIds().size() > 0);
    }

    @Test
    public void rootTestGetVariantsByVariantCoreStringWithTranslatedContig() throws URISyntaxException {
        given(contigAliasService.translateContigFromInsdc(VARIANT.getChromosome(), ContigNamingConvention.ENA_SEQUENCE_NAME))
                .willReturn("2");

        String url = "/v2/variants/" + CHROMOSOME + ":71822:C:G?species=mmusculus&assembly=grcm38&contigNamingConvention=ENA_SEQUENCE_NAME";
        ResponseEntity<VariantWithSamplesAndAnnotation> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<VariantWithSamplesAndAnnotation>() {
                });
        VariantWithSamplesAndAnnotation variantWithSamplesAndAnnotation = response.getBody();
        assertEquals("2", variantWithSamplesAndAnnotation.getChromosome());
        assertEquals(0, variantWithSamplesAndAnnotation.getSourceEntries().size());
        assertNull(variantWithSamplesAndAnnotation.getAnnotation());
        assertTrue(variantWithSamplesAndAnnotation.getIds().size() > 0);
    }

    @Test
    public void rootTestGetVariantsByNonExistingVariantCoreString() throws URISyntaxException {
        String url = "/v2/variants/" + NON_EXISTING_CHROMOSOME + ":71822:C:G?species=mmusculus&assembly=grcm38";
        testForNonExistingHelper(url);
    }

    private void testForNonExistingHelper(String url) {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void rootTestForError() throws URISyntaxException {
        String url = "/v2/variants/13:32889669:C:T?species=&assembly=grcm38";
        assertEquals("Please specify a species", testForErrorHelper(url));
    }

    private String testForErrorHelper(String url) {
        return WSTestHelpers.testRestTemplateHelperForError(url, restTemplate);
    }

    @Test
    public void annotationEndPointTestExisting() throws URISyntaxException {
        String url = "/v2/variants/" + CHROMOSOME + ":60100:A:T/annotations?species=mmusculus&assembly=grcm38";
        ResponseEntity<Annotation> annotations = restTemplate.exchange(url, HttpMethod.GET,
                null, new ParameterizedTypeReference<Annotation>() {
                });
        assertEquals(HttpStatus.OK, annotations.getStatusCode());
        assertFalse(annotations.getBody().getChromosome().isEmpty());
    }

    @Test
    public void annotationEndPointTestNonExisting() throws URISyntaxException {
        String url = "/v2/variants/100:0:C:T/annotations?species=mmusculus&assembly=grcm38";
        testForNonExistingHelper(url);
    }

    @Test
    public void annotationEndpointTestForError() throws URISyntaxException {
        String url = "/v2/variants/13:32889669:C:T/annotations?species=mmusculus&assembly=grcm38&" +
                "annot-vep-version=1";
        assertEquals("Please specify either both annotation VEP version and annotation VEP cache version, " +
                "or neither", testForErrorHelper(url));
    }

    @Test
    public void sourceEntriesEndPointTestExisting() throws URISyntaxException {
        String url = "/v2/variants/" + CHROMOSOME + ":60100:A:T/sources?species=mmusculus&assembly=grcm38";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        Configuration configuration = Configuration.defaultConfiguration()
                .jsonProvider(new JacksonJsonProvider())
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .addOptions(Option.SUPPRESS_EXCEPTIONS);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<VariantSourceEntryWithSampleNames> sources = JsonPath.using(configuration).parse(response.getBody())
                .read("$['_embedded']['variantSourceEntryWithSampleNamesList']", new TypeRef<List<VariantSourceEntryWithSampleNames>>() {
                });
        assertFalse(sources.get(0).getFileId().isEmpty());
    }

    @Test
    public void sourceEntriesEndPointTestNonExisting() throws URISyntaxException {
        String url = "/v2/variants/100:0:C:T/sources?species=mmusculus&assembly=grcm38";
        testForNonExistingHelper(url);
    }

    @Test
    public void sourceEntriesEndpointTestForError() throws URISyntaxException {
        String url = "/v2/variants/13:32889669:C:T/sources?species=mmusculus&assembly=grcm38&" +
                "annot-vep-version=1";
        assertEquals("Please specify either both annotation VEP version and annotation VEP cache version, " +
                "or neither", testForErrorHelper(url));
    }
}
