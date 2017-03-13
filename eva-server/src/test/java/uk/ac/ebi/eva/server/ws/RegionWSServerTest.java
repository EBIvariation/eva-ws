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

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.opencb.biodata.models.feature.Region;
import org.opencb.datastore.core.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;
import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;

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

        List<Region> oneRegion = (List<Region>) argThat(hasSize(1));
        BDDMockito.given(variantEntityRepository.findByRegionsAndComplexFilters(oneRegion, any(), any(), any()))
                .willReturn(Collections.singletonList(variantEntity));

        List<Region> twoRegions = (List<Region>) argThat(hasSize(2));
        BDDMockito.given(variantEntityRepository.findByRegionsAndComplexFilters(twoRegions, any(), any(), any()))
                .willReturn(Arrays.asList(variantEntity, variantEntity));
    }

    @Test
    public void testGetVariantsByRegion() throws URISyntaxException {
        testGetVariantsByRegionHelper("20:60000-62000", 1);
    }

    @Test
    public void testGetVariantsByRegions() throws URISyntaxException {
        testGetVariantsByRegionHelper("20:60000-61000,20:61500-62500", 2);
    }

    private void testGetVariantsByRegionHelper(String testRegion, int expectedVariants) throws URISyntaxException {
        String url = "/v1/segments/" + testRegion + "/variants?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse> response = restTemplate.getForEntity(url, QueryResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<Map<String, List>> queryResponse = (List<Map<String, List>>) response.getBody().getResponse();
        assertEquals(1, queryResponse.size());

        List<Map> results = (List<Map>) queryResponse.get(0).get("result");
        assertEquals(expectedVariants, results.size());

        for (Map variantEntity : results) {
        assertTrue(variantEntity.containsKey("chromosome"));
        assertTrue(variantEntity.containsKey("start"));
        assertTrue(variantEntity.containsKey("end"));
        assertTrue(variantEntity.containsKey("reference"));
        assertTrue(variantEntity.containsKey("alternate"));
        }
    }

}
