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
import uk.ac.ebi.eva.commons.core.models.Annotation;
import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.ws.VariantSourceEntryWithSampleNames;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;

/**
 * Tests for VariantWSServer
 */
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
    private VariantWithSamplesAndAnnotationsService variantEntityRepository;

    @Before
    public void setUp() throws Exception {

        VARIANT.addId("randomID");
        VARIANT.setAnnotation(new Annotation(CHROMOSOME, 0, 0, null, null, null, null));
        VARIANT.addSourceEntry(new VariantSourceEntryWithSampleNames("fid", "sid", null, null, null, null, null));
        List<VariantWithSamplesAndAnnotation> variantEntities = Collections.singletonList(VARIANT);

        given(variantEntityRepository
                .findByChromosomeAndStartAndReferenceAndAlternate(eq(CHROMOSOME), anyInt(), any(), any(), any()))
                .willReturn(variantEntities);
    }

    @Test
    public void rootTestGetVariantsByVariantId() throws URISyntaxException {
        List<VariantWithSamplesAndAnnotation> variantWithSamplesAndAnnotations = variantWsHelper(CHROMOSOME
                + ":71822:C:G");
        assertTrue(variantWithSamplesAndAnnotations.size() > 0);
        assertTrue(variantWithSamplesAndAnnotations.get(0).getSourceEntries().size() == 0);
        assertNull(variantWithSamplesAndAnnotations.get(0).getAnnotation());
        assertTrue(variantWithSamplesAndAnnotations.get(0).getIds().size() > 0);
    }

    private List<VariantWithSamplesAndAnnotation> variantWsHelper(String testVariantId) {
        String url = "/v2/variants/" + testVariantId + "?species=mmusculus_grcm38";
        return WSTestHelpers.testRestTemplateHelper(url, restTemplate);
    }

    @Test
    public void rootTestGetVariantsByNonExistingVariantId() throws URISyntaxException {
        assertEquals(0, variantWsHelper(NON_EXISTING_CHROMOSOME+":60100:A:T").size());
    }

    @Test
    public void rootTestForError() throws URISyntaxException {
        String url;
        url = "/v2/variants/13:32889669:C:T?species=";
        assertEquals("Please specify a species", testForErrorHelper(url));
        url = "/v2/variants/13:32889669:C:T?species=mmusculus_grcm38&annot-vep-version=1";
        assertEquals("Please specify either both annotation VEP version and annotation VEP cache version, " +
                "or neither", testForErrorHelper(url));
    }

    private String testForErrorHelper(String url) {
        return WSTestHelpers.testRestTemplateHelperForError(url, restTemplate);
    }

    @Test
    public void annotationEndPointTestExisting() throws URISyntaxException {
        String url = "/v2/variants/" + CHROMOSOME + ":60100:A:T/annotations?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<Annotation>>> annotations = restTemplate.exchange(url, HttpMethod.GET,
                null, new ParameterizedTypeReference<QueryResponse<QueryResult<Annotation>>>() {
                });
        assertEquals(HttpStatus.OK, annotations.getStatusCode());
        assertFalse(annotations.getBody().getResponse().get(0).getResult().get(0).getChromosome().isEmpty());
    }

    @Test
    public void annotationEndPointTestNonExisting() throws URISyntaxException {
        String url = "/v2/variants/100:0:C:T/annotations?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<Annotation>>> annotations = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<Annotation>>>() {
                });
        assertEquals(HttpStatus.OK, annotations.getStatusCode());
        assertTrue(annotations.getBody().getResponse().get(0).getResult().size() == 0);
    }

    @Test
    public void annotationEndpointTestForError() throws URISyntaxException {
        String url;
        url = "/v2/variants/13:32889669:C:T/annotations?species=";
        assertEquals("Please specify a species", testForErrorHelper(url));
        url = "/v2/variants/13:32889669:C:T/annotations?species=mmusculus_grcm38&" +
                "annot-vep-version=1";
        assertEquals("Please specify either both annotation VEP version and annotation VEP cache version, " +
                "or neither", testForErrorHelper(url));
    }

    @Test
    public void sourceEntriesEndPointTestExisting() throws URISyntaxException {
        String url = "/v2/variants/" + CHROMOSOME + ":60100:A:T/source-Entries?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<VariantSourceEntryWithSampleNames>>> annotations = restTemplate.
                exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference
                        <QueryResponse<QueryResult<VariantSourceEntryWithSampleNames>>>() {
                });
        assertEquals(HttpStatus.OK, annotations.getStatusCode());
        assertFalse(annotations.getBody().getResponse().get(0).getResult().get(0).getFileId().isEmpty());
    }

    @Test
    public void sourceEntriesEndPointTestNonExisting() throws URISyntaxException {
        String url = "/v2/variants/100:0:C:T/source-Entries?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<Annotation>>> annotations = restTemplate.exchange(url, HttpMethod.GET,
                null, new ParameterizedTypeReference<QueryResponse<QueryResult<Annotation>>>() {
                });
        assertEquals(HttpStatus.OK, annotations.getStatusCode());
        assertTrue(annotations.getBody().getResponse().get(0).getResult().size() == 0);
    }

    @Test
    public void sourceEntriesEndpointTestForError() throws URISyntaxException {
        String url;
        url = "/v2/variants/13:32889669:C:T/source-Entries?species=";
        assertEquals("Please specify a species", testForErrorHelper(url));
        url = "/v2/variants/13:32889669:C:T/source-Entries?species=mmusculus_grcm38&" +
                "annot-vep-version=1";
        assertEquals("Please specify either both annotation VEP version and annotation VEP cache version, " +
                "or neither", testForErrorHelper(url));
    }

    @Test
    public void sourceEntryEndPointTestExisting() throws URISyntaxException {
        String url = "/v2/variants/" + CHROMOSOME + ":60100:A:T/source-Entries/sid_fid?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<VariantSourceEntryWithSampleNames>>> annotations = restTemplate.
                exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference
                        <QueryResponse<QueryResult<VariantSourceEntryWithSampleNames>>>() {
                });
        assertEquals(HttpStatus.OK, annotations.getStatusCode());
        assertFalse(annotations.getBody().getResponse().get(0).getResult().get(0).getFileId().isEmpty());
    }

    @Test
    public void sourceEntryEndPointTestNonExisting() throws URISyntaxException {
        String url = "/v2/variants/100:0:C:T/source-Entries/sid_fid?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<Annotation>>> annotations = restTemplate.exchange(url, HttpMethod.GET,
                null, new ParameterizedTypeReference<QueryResponse<QueryResult<Annotation>>>() {
                });
        assertEquals(HttpStatus.OK, annotations.getStatusCode());
        assertTrue(annotations.getBody().getResponse().get(0).getResult().size() == 0);
    }

    @Test
    public void sourceEntryEndpointTestForError() throws URISyntaxException {
        String url;
        url = "/v2/variants/13:32889669:C:T/source-Entries/PRJEB5829_ERZ019958?species=";
        assertEquals("Please specify a species", testForErrorHelper(url));
        url = "/v2/variants/13:32889669:C:T/source-Entries?species=mmusculus_grcm38&" +
                "annot-vep-version=1";
        assertEquals("Please specify either both annotation VEP version and annotation VEP cache version, " +
                "or neither", testForErrorHelper(url));
    }
}
