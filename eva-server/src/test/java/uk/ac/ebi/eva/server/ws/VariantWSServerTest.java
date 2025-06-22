/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2016 EMBL - European Bioinformatics Institute
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
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigAliasChromosome;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigNamingConvention;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.lib.utils.TaxonomyUtils;
import uk.ac.ebi.eva.server.ws.contigalias.ContigAliasService;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;


/**
 * Tests for VariantWSServer
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VariantWSServerTest {

    private static final String CHROMOSOME = "existingChromosome";

    private static final String VARIANT_ID = "existingId";

    private static final String NON_EXISTING_VARIANT_ID = "notARealId";

    private static final String NON_EXISTING_CHROMOSOME = "notARealChromosome";

    private static final String MAIN_ID = "rs1";

    private static final VariantWithSamplesAndAnnotation VARIANT = new VariantWithSamplesAndAnnotation("1", 1000, 1005,
                                                                                                       "A", "T",
                                                                                                       MAIN_ID);

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantWithSamplesAndAnnotationsService variantEntityRepository;

    @MockBean
    private ContigAliasService contigAliasService;

    @MockBean
    private TaxonomyUtils taxonomyUtils;

    @Before
    public void setUp() throws Exception {
        List<VariantWithSamplesAndAnnotation> variantEntities = Collections.singletonList(VARIANT);
        ContigAliasChromosome contigAliasChromosome = new ContigAliasChromosome();
        contigAliasChromosome.setInsdcAccession(CHROMOSOME);

        given(variantEntityRepository
                .findByChromosomeAndStartAndReferenceAndAlternate(eq(CHROMOSOME), anyLong(), any(), any(), any()))
                .willReturn(variantEntities);

        given(variantEntityRepository.findByIdsAndComplexFilters(eq(Arrays.asList(VARIANT_ID)), any(), any(), any(), any()))
                .willReturn(variantEntities);

        given(contigAliasService.getVariantsWithTranslatedContig(Collections.singletonList(VARIANT), null))
                .willReturn(Collections.singletonList(VARIANT));

        given(contigAliasService.getVariantsWithTranslatedContig(Collections.singletonList(VARIANT), ContigNamingConvention.NO_REPLACEMENT))
                .willReturn(Collections.singletonList(VARIANT));

        given(contigAliasService.getUniqueInsdcChromosomeByName(eq(CHROMOSOME), eq("GCA_000001635.2"),
                eq(ContigNamingConvention.NO_REPLACEMENT))).willReturn(contigAliasChromosome);

        given(contigAliasService.getUniqueInsdcChromosomeByName(eq(CHROMOSOME), eq("GCA_000001635.2"),
                eq(null))).willReturn(contigAliasChromosome);

        given(contigAliasService.getVariantsWithTranslatedContig(eq(variantEntities), eq(contigAliasChromosome),
                eq(ContigNamingConvention.NO_REPLACEMENT))).willReturn(variantEntities);

        given(taxonomyUtils.getAssemblyAccessionForAssemblyCode("grcm38")).willReturn(Optional.of("GCA_000001635.2"));

    }

    @Test
    public void testGetVariantById() {
        testGetVariantByIdRegionHelper(VARIANT_ID, VARIANT, null);
    }

    @Test
    public void testGetVariantByIdWithContigTranslation() {
        VariantWithSamplesAndAnnotation variantWithTranslatedContig = new VariantWithSamplesAndAnnotation("X",
                1000, 1005, "A", "T", MAIN_ID);
        given(contigAliasService.getVariantsWithTranslatedContig(Collections.singletonList(VARIANT), ContigNamingConvention.ENA_SEQUENCE_NAME))
                .willReturn(Collections.singletonList(variantWithTranslatedContig));
        testGetVariantByIdRegionHelper(VARIANT_ID, variantWithTranslatedContig, ContigNamingConvention.ENA_SEQUENCE_NAME);
    }

    @Test
    public void testGetVariantByRegion() {
        testGetVariantByIdRegionHelper(CHROMOSOME + ":71822:C:G", VARIANT, ContigNamingConvention.NO_REPLACEMENT);
    }

    private void testGetVariantByIdRegionHelper(String testString, VariantWithSamplesAndAnnotation expectedVariant,
                                                ContigNamingConvention contigNamingConvention) {
        String url = "/v1/variants/" + testString + "/info?species=mmusculus_grcm38";
        if (contigNamingConvention != null) {
            url += "&contigNamingConvention=" + contigNamingConvention;
        }
        ResponseEntity<QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>>>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantWithSamplesAndAnnotation> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(1, results.size());

        assertEquals(expectedVariant, results.get(0));
        assertEquals(MAIN_ID, results.get(0).getMainId());
        assertEquals(expectedVariant.getChromosome(), results.get(0).getChromosome());
    }

    @Test
    public void testGetVariantByIdDoesntExist() throws URISyntaxException {
        testGetVariantByIdRegionDoesntExistHelper(NON_EXISTING_VARIANT_ID);
    }

    @Test
    public void testGetVariantByRegionDoesntExist() throws URISyntaxException {
        testGetVariantByIdRegionDoesntExistHelper(NON_EXISTING_CHROMOSOME + ":71821:C:G");
    }

    private void testGetVariantByIdRegionDoesntExistHelper(String testString) throws URISyntaxException {
        String url = "/v1/variants/" + testString + "/info?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>>>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantWithSamplesAndAnnotation> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(0, results.size());
    }

    @Test
    public void testCheckVariantExistsDoesExistRegion() throws URISyntaxException {
        assertTrue(testCheckVariantExistsHelper(CHROMOSOME + ":1:C:G"));
    }

    @Test
    public void testCheckVariantExistsDoesntExistRegion() throws URISyntaxException {
        assertFalse(testCheckVariantExistsHelper(NON_EXISTING_CHROMOSOME + ":1:C:G"));
    }

    @Test
    public void testCheckVariantExistsDoesExistId() throws URISyntaxException {
        assertTrue(testCheckVariantExistsHelper(VARIANT_ID));
    }

    @Test
    public void testCheckVariantExistsDoesntExistId() throws URISyntaxException {
        assertFalse(testCheckVariantExistsHelper(NON_EXISTING_VARIANT_ID));
    }

    private Boolean testCheckVariantExistsHelper(String testIdRegion) throws URISyntaxException {
        String url = "/v1/variants/" + testIdRegion + "/exists?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<Boolean>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<Boolean>>>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<Boolean>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<Boolean> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(1, results.size());
        return results.get(0);
    }

    @Test
    public void testCountVariants() throws URISyntaxException {
        Long expectedNumberOfVariants = new Long(0);

        String url = "/v1/variants/count";
        ResponseEntity<QueryResponse<QueryResult<Long>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<Long>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<Long>> queryResponse = response.getBody();
        assertEquals(expectedNumberOfVariants, queryResponse.getResponse().get(0).getResult().get(0));
    }

}
