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
import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegionWSServerV2Test {

    private static final String MAIN_ID = "rs1";

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantWithSamplesAndAnnotationsService service;

    @Before
    public void setUp() throws Exception {
        VariantWithSamplesAndAnnotation variantEntity = new VariantWithSamplesAndAnnotation("chr1", 1000, 1005,
                "reference", "alternate", MAIN_ID);

        List<Region> oneRegion = Collections.singletonList(new Region("20", 60000L, 62000L));
        given(service.findByRegionsAndComplexFilters(eq(oneRegion), any(), any(), any(), any()))
                .willReturn(Collections.singletonList(variantEntity));
        given(service.countByRegionsAndComplexFilters(eq(oneRegion), any())).willReturn(1l);

        List<Region> twoRegions = Arrays.asList(new Region("20", 60000L, 61000L),
                new Region("20", 61500L, 62500L));

        given(service.findByRegionsAndComplexFilters(eq(twoRegions), any(), any(), any(), any()))
                .willReturn(Arrays.asList(variantEntity, variantEntity));
        given(service.countByRegionsAndComplexFilters(eq(twoRegions), any())).willReturn(2l);

        given(service
                .findByRegionsAndComplexFilters(not(or(eq(oneRegion), eq(twoRegions))), any(), any(), any(), any()))
                .willReturn(Collections.emptyList());
        given(service.countByRegionsAndComplexFilters(not(or(eq(oneRegion), eq(twoRegions))), any())).willReturn(0l);
    }

    @Test
    public void testGetVariantsByExistingRegion() throws URISyntaxException {
        testGetVariantsByRegionHelper("20:60000-62000", 1, HttpStatus.OK);
    }

    private void testGetVariantsByRegionHelper(String testRegion, int expectedVariants, HttpStatus status) throws
            URISyntaxException {
        List<Variant> results = regionWsHelper(testRegion, status);
        assertEquals(expectedVariants, results.size());

        for (Variant variantEntity : results) {
            assertFalse(variantEntity.getChromosome().isEmpty());
            assertFalse(variantEntity.getReference().isEmpty());
            assertFalse(variantEntity.getAlternate().isEmpty());
            assertNotEquals(0, variantEntity.getStart());
            assertNotEquals(0, variantEntity.getEnd());
            assertEquals(MAIN_ID, variantEntity.getMainId());
        }
    }

    private List<Variant> regionWsHelper(String testRegion, HttpStatus status) {
        String url = "/v2/regions/" + testRegion + "?species=mmusculus&assembly=grcm38";
        ResponseEntity<List<Variant>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Variant>>() {
                });
        assertEquals(status, response.getStatusCode());
        List<Variant> queryResponse = response.getBody();
        return queryResponse;
    }

    @Test
    public void testGetVariantsByExistingRegions() throws URISyntaxException {
        testGetVariantsByRegionHelper("20:60000-61000,20:61500-62500", 2, HttpStatus.OK);
    }

    @Test
    public void testGetVariantsByNonExistingRegion() throws URISyntaxException {
        testGetVariantsByRegionHelper("21:8000-9000", 0, HttpStatus.NOT_FOUND);
    }

    @Test
    public void testGetVariantsByNonExistingRegions() throws URISyntaxException {
        testGetVariantsByRegionHelper("21:8000-9000,21:8000-9000", 0, HttpStatus.NOT_FOUND);
    }
}
