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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.ws.VariantSourceEntryWithSampleNames;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.models.ga4gh.GASearchVariantsResponse;
import uk.ac.ebi.eva.lib.models.ga4gh.GAVariantFactory;

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

    private VariantWithSamplesAndAnnotation variant;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantWithSamplesAndAnnotationsService variantEntityRepository;

    @Before
    public void setUp() throws Exception {
        variant = new VariantWithSamplesAndAnnotation("1", 1000, 1005, "A", "C");
        variant.setIds(Collections.singleton("1_1000_A_C"));
        variant.addSourceEntry(new VariantSourceEntryWithSampleNames("FILE_ID", "STUDY_ID", null, null, null, null,
                null));
        List<VariantWithSamplesAndAnnotation> variantEntities = Collections.singletonList(variant);

        Region region = new Region("1", 500, 2000);

        given(variantEntityRepository.findByRegionsAndComplexFilters(eq(Collections.singletonList(region)),
                                                                     any(),
                                                                     any(),
                                                                     any(),
                                                                     any()))
                .willReturn(variantEntities);
        given(variantEntityRepository.countByRegionsAndComplexFilters(eq(Collections.singletonList(region)),
                any()))
                .willReturn(Long.valueOf(variantEntities.size()));
    }

    @Test
    public void testRegionWithVariants() throws Exception {
        GASearchVariantsResponse gaSearchVariantsResponse = testVariantWsHelper("1", 500, 2000, new ArrayList<>(),
                "0", 10);
        assertEquals(1, gaSearchVariantsResponse.getVariants().size());
        assertEquals(GAVariantFactory.create(Collections.singletonList(variant)),
                gaSearchVariantsResponse.getVariants());
    }

    @Test
    public void testRegionWithNoVariants() throws Exception {
        GASearchVariantsResponse gaSearchVariantsResponse = testVariantWsHelper("2", 5000, 10000, new ArrayList<>(),
                "0", 10);
        assertEquals(0, gaSearchVariantsResponse.getVariants().size());
    }

    private GASearchVariantsResponse testVariantWsHelper(String chromosome, int start, int end,
                                                         List<String> variantSetIds, String pageToken, int pageSize) {

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