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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.eva.commons.core.models.FeatureCoordinates;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigNamingConvention;
import uk.ac.ebi.eva.commons.mongodb.services.FeatureService;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.server.ws.contigalias.ContigAliasService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FeaturesWSServerTest {

    private static final String FEATURE_NAME = "FBXO2";

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FeatureService service;

    @MockBean
    private ContigAliasService contigAliasService;

    FeatureCoordinates exampleFeature = new FeatureCoordinates("id", FEATURE_NAME, "feature", "chr", 0, 1);

    @BeforeEach
    public void setup() throws URISyntaxException, IOException {
        given(service.findByIdOrName(FEATURE_NAME, FEATURE_NAME))
                .willReturn(Collections.singletonList(exampleFeature));
        given(contigAliasService.getFeatureCoordinatesWithTranslatedContig(Collections.singletonList(exampleFeature), null))
                .willReturn(Collections.singletonList(exampleFeature));
    }

    @Test
    public void testGetFeatures() {
        String url = "/v1/features/" + FEATURE_NAME + "?species=hsapiens_grch37";
        ResponseEntity<QueryResponse<QueryResult<FeatureCoordinates>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<FeatureCoordinates>>>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<FeatureCoordinates>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<FeatureCoordinates> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(1, results.size());

        assertEquals(FEATURE_NAME, results.get(0).getName());
    }

    @Test
    public void testGetFeaturesWithTranslatedContig() {
        FeatureCoordinates translatedFeature = new FeatureCoordinates("id", FEATURE_NAME, "feature", "1", 0, 1);
        given(contigAliasService.getFeatureCoordinatesWithTranslatedContig(Collections.singletonList(exampleFeature),
                ContigNamingConvention.ENA_SEQUENCE_NAME))
                .willReturn(Collections.singletonList(translatedFeature));

        String url = "/v1/features/" + FEATURE_NAME + "?species=hsapiens_grch37&contigNamingConvention="
                + ContigNamingConvention.ENA_SEQUENCE_NAME;
        ResponseEntity<QueryResponse<QueryResult<FeatureCoordinates>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<FeatureCoordinates>>>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<FeatureCoordinates>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());
        List<FeatureCoordinates> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(1, results.size());
        assertEquals(FEATURE_NAME, results.get(0).getName());
        assertEquals("1", results.get(0).getChromosome());
    }

    @Test
    public void testGetFeaturesWithEmptySpeciesShouldFail() {
        String url = "/v1/features/" + FEATURE_NAME + "?species=";
        ResponseEntity<QueryResponse<QueryResult<FeatureCoordinates>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<FeatureCoordinates>>>() {
                });
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        QueryResponse<QueryResult<FeatureCoordinates>> queryResponse = response.getBody();
        assertEquals("Please specify a species", queryResponse.getError());
        assertEquals(0, queryResponse.getResponse().size());
    }

    @Test
    public void testGetFeaturesWithoutSpeciesShouldFail() {
        String url = "/v1/features/" + FEATURE_NAME;
        ResponseEntity<QueryResponse<QueryResult<FeatureCoordinates>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<FeatureCoordinates>>>() {
                });
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        QueryResponse<QueryResult<FeatureCoordinates>> queryResponse = response.getBody();
        assertEquals(0, queryResponse.getResponse().size());
    }

}
