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
import org.opencb.biodata.models.feature.Region;
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

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;
import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

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
public class RegionWSServerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantEntityRepository variantEntityRepository;

    @Before
    public void setUp() throws Exception {
        VariantEntity variantEntity = new VariantEntity("chr1", 1000, 1005, "reference", "alternate");

        List<Region> oneRegion = Arrays.asList(
                new Region("20", 60000, 62000));
        given(variantEntityRepository.findByRegionsAndComplexFilters(eq(oneRegion), any(), any(), any()))
                .willReturn(Collections.singletonList(variantEntity));

        List<Region> twoRegions = Arrays.asList(
                new Region("20", 60000, 61000),
                new Region("20", 61500, 62500));
        given(variantEntityRepository.findByRegionsAndComplexFilters(eq(twoRegions), any(), any(), any()))
                .willReturn(Arrays.asList(variantEntity, variantEntity));

        given(variantEntityRepository
                .findByRegionsAndComplexFilters(not(or(eq(oneRegion), eq(twoRegions))), any(), any(), any()))
                .willReturn(Collections.emptyList());
    }

    @Test
    public void testGetVariantsByRegion() throws URISyntaxException {
        testGetVariantsByRegionHelper("20:60000-62000", 1);
    }

    @Test
    public void testGetVariantsByRegions() throws URISyntaxException {
        testGetVariantsByRegionHelper("20:60000-61000,20:61500-62500", 2);
    }

    @Test
    public void testGetVariantsByNonExistingRegion() throws URISyntaxException {
        testGetVariantsByRegionHelper("21:8000-9000", 0);
    }

    private void testGetVariantsByRegionHelper(String testRegion, int expectedVariants) throws URISyntaxException {
        List<VariantEntity> results = regionWsHelper(testRegion);
        assertEquals(expectedVariants, results.size());

        for (VariantEntity variantEntity : results) {
            assertFalse(variantEntity.getChromosome().isEmpty());
            assertFalse(variantEntity.getReference().isEmpty());
            assertFalse(variantEntity.getAlternate().isEmpty());
            assertNotEquals(0, variantEntity.getStart());
            assertNotEquals(0, variantEntity.getEnd());
        }
    }

    private List<VariantEntity> regionWsHelper(String testRegion) {
        String url = "/v1/segments/" + testRegion + "/variants?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<VariantEntity>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantEntity>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<VariantEntity>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        return queryResponse.getResponse().get(0).getResult();
    }

}
