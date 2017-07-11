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
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GA4GHBeaconWSServerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantWithSamplesAndAnnotationsService service;

    @Before
    public void setUp() throws Exception {
        VariantWithSamplesAndAnnotation variant = new VariantWithSamplesAndAnnotation("1", 1000, 1005, "reference", "alternate");
        List<VariantWithSamplesAndAnnotation> variantEntities = Collections.singletonList(variant);

        given(service.findByChromosomeAndStartAndAltAndStudyIn(eq("1"), anyInt(), any(), any(), any()))
                .willReturn(variantEntities);
    }

    @Test
    public void testAltAlleleExistsBeacon() throws Exception {
        assertTrue(testBeaconHelper("1", 1000, "alternate", new ArrayList<>()));
    }

    @Test
    public void testAltAlleleDoesntExistsBeacon() throws Exception {
        assertFalse(testBeaconHelper("2", 2000, "alternateOther", new ArrayList<>()));
    }

    private boolean testBeaconHelper(String chromosome, int start, String allele, List<String> datasetIds) {
        String url = String.format("/v1/ga4gh/beacon?referenceName=%s&start=%d&allele=%s&datasetIds=%s",
                chromosome,
                start,
                allele,
                String.join(",", datasetIds));
        ResponseEntity<GA4GHBeaconResponse> response = restTemplate.getForEntity(
                url, GA4GHBeaconResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        GA4GHBeaconResponse ga4ghBeaconResponse = response.getBody();
        return ga4ghBeaconResponse.isExists();
    }
}