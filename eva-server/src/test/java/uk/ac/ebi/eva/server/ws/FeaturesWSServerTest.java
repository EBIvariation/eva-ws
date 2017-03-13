/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2015 EMBL - European Bioinformatics Institute
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
import org.mockito.BDDMockito;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.models.data.FeatureCoordinates;
import uk.ac.ebi.eva.lib.repository.FeatureRepository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FeaturesWSServerTest {

    private static final String FEATURE_NAME = "FBXO2";

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FeatureRepository featureRepository;

    @Before
    public void setup() throws URISyntaxException, IOException, IllegalOpenCGACredentialsException {
        FeatureCoordinates exampleFeature = new FeatureCoordinates("id", FEATURE_NAME, "feature", "chr", 0, 1);
        BDDMockito.given(featureRepository.findByIdOrName(FEATURE_NAME, FEATURE_NAME)).willReturn(
                Collections.singletonList(exampleFeature));
    }

    @Test
    public void testGetGenes() throws URISyntaxException {
        ResponseEntity<QueryResponse> response = restTemplate.getForEntity(
                "/v1/features/" + FEATURE_NAME + "?species=hsapiens_grch37", QueryResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<Map<String, List>> queryResponse = (List<Map<String, List>>) response.getBody().getResponse();
        assertEquals(1, queryResponse.size());

        List<Map<String, String>> results = (List<Map<String, String>>) queryResponse.get(0).get("result");
        assertEquals(1, results.size());
        assertEquals(FEATURE_NAME, results.get(0).get("name"));
    }

    @Test
    public void testGetGenesEmptySpecies() throws URISyntaxException {
        ResponseEntity<QueryResponse> response = restTemplate.getForEntity(
                "/v1/features/" + FEATURE_NAME + "?species=", QueryResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        QueryResponse queryResponse = response.getBody();
        assertEquals("Please specify a species", queryResponse.getError());

        List<Map<String, String>> results = (List<Map<String, String>>) queryResponse.getResponse();
        assertEquals(0, results.size());
    }

    @Test
    public void testGetGenesWithoutSpecies() throws URISyntaxException {
        ResponseEntity<QueryResponse> response = restTemplate.getForEntity("/v1/features/" + FEATURE_NAME,
                QueryResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        QueryResponse queryResponse = response.getBody();
        assertNull(queryResponse.getResponse());
    }

}
