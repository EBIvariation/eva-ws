/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.server.ws.ga4gh;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencb.biodata.ga4gh.GASearchVariantsResponse;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.VariantSourceEntry;
import org.opencb.biodata.models.variant.ga4gh.GAVariantFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;
import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GA4GHVariantWSServerTest {

    private static VariantEntity VARIANT = new VariantEntity("1", 1000, 1005, "A", "C");

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantEntityRepository variantEntityRepository;

    @Before
    public void setUp() throws Exception {
        VARIANT.setIds(Collections.singleton("1_1000_A_C"));
        VARIANT.addSourceEntry(new VariantSourceEntry("FILE_ID", "STUDY_ID"));
        List<VariantEntity> variantEntities = Collections.singletonList(VARIANT);

        Region region = new Region("1", 500, 2000);

        given(variantEntityRepository.findByRegionsAndComplexFilters(eq(Collections.singletonList(region)),
                                                                     any(),
                                                                     any(),
                                                                     any()))
                .willReturn(variantEntities);
        given(variantEntityRepository.countByRegionsAndComplexFilters(eq(Collections.singletonList(region)),
                                                                     any()))
                .willReturn(new Long(variantEntities.size()));
    }

    @Test
    public void testRegionWithVariants() throws Exception {
        GASearchVariantsResponse gaSearchVariantsResponse = testVariantWsHelper("1", 500, 2000, new ArrayList<>(), "0", 10);
        assertEquals(1, gaSearchVariantsResponse.getVariants().size());
        assertEquals(GAVariantFactory.create(Collections.singletonList(VARIANT)), gaSearchVariantsResponse.getVariants());
    }

    private GASearchVariantsResponse testVariantWsHelper(String chromosome, int start, int end, List<String> variantSetIds,
                                        String pageToken, int pageSize) {

        String url = String.format("/v1/ga4gh/variants/search?" +
                                           "referenceName=%s" +
                                           "&start=%d" +
                                           "&end=%d" +
                                           "&variantSetIds=%s" +
                                           "&pageToken=%s" +
                                           "&pageSize=%d",
                                   chromosome,
                                   start,
                                   end,
                                   String.join(",", variantSetIds),
                                   pageToken,
                                   pageSize);

        ResponseEntity<GASearchVariantsResponse> response = restTemplate.getForEntity(
                url, GASearchVariantsResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        return response.getBody();
    }

}