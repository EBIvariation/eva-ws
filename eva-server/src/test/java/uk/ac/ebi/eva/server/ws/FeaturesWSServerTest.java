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
import org.opencb.datastore.core.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.models.data.FeatureCoordinates;
import uk.ac.ebi.eva.lib.repository.FeatureRepository;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;


/**
 * This test, just as FilesWSServerTest, needs a DB named "eva_mmusculus_grcm38" with a collection "features".
 * There should be at least one document with "_id" or "name" called "FBXO25".
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FeaturesWSServerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private DBAdaptorConnector dbAdaptorConnector;

    @MockBean
    private FeatureRepository featureRepository;

    private static final String FEATURE_NAME = "FBXO2";

    @Before
    public void setup() {
        FeatureCoordinates exampleFeature = new FeatureCoordinates("id", FEATURE_NAME, "feature", "chr", 0, 1);
        BDDMockito.given(featureRepository.findByIdOrName(anyString(), anyString())).willReturn(
                Collections.singletonList(exampleFeature));
    }

    @Test
    public void testGetGenes() throws URISyntaxException {
        ResponseEntity<QueryResponse> response = restTemplate.getForEntity("/v1/features/" + FEATURE_NAME,
                QueryResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List queryResponse = response.getBody().getResponse();
        assertEquals(1, queryResponse.size());

        List<FeatureCoordinates> results = ((QueryResult<FeatureCoordinates>) queryResponse.get(0)).getResult();
        assertEquals(1, results.size());
        assertEquals(FEATURE_NAME, results.get(0).getName());
    }

}
