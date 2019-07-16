/*
 * Copyright 2019 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import uk.ac.ebi.eva.commons.core.models.FeatureCoordinates;
import uk.ac.ebi.eva.commons.mongodb.services.FeatureService;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GeneWSServerV2Test {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FeatureService service;

    private String GENE_ID = "GeneId";

    @Before
    public void setUp() throws Exception {
        FeatureCoordinates feature = new FeatureCoordinates(GENE_ID, "id", "feature", "chromosome", 1l, 2l);

        List<FeatureCoordinates> featureCoordinate = Arrays.asList(feature);
        List<String> geneId = Arrays.asList(GENE_ID);
        given(service.findAllByGeneIdsOrGeneNames(eq(geneId), eq(geneId))).willReturn(featureCoordinate);

        List<FeatureCoordinates> featureCoordinates = Arrays.asList(feature, feature);
        List<String> geneIds = Arrays.asList(GENE_ID, GENE_ID);
        given(service.findAllByGeneIdsOrGeneNames(eq(geneIds), eq(geneIds))).willReturn(featureCoordinates);
    }

    @Test
    public void testGeneIdExisting() {
        testGeneIdHelper(Arrays.asList(GENE_ID), HttpStatus.OK, 1);
    }

    private void testGeneIdHelper(List<String> geneIds, HttpStatus status, int size) {
        String url = "/v2/genes/" + String.join(",", geneIds) + "?species=hsapiens&assembly=grch37";
        ResponseEntity<List<FeatureCoordinates>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<FeatureCoordinates>>() {
                });
        assertEquals(status, response.getStatusCode());
        assertEquals(size, response.getBody().size());
        for (int i = 0; i < response.getBody().size(); i++) {
            assertEquals(geneIds.get(i), response.getBody().get(i).getId());
        }
    }

    @Test
    public void testGendIdsExisiting() {
        testGeneIdHelper(Arrays.asList(GENE_ID, GENE_ID), HttpStatus.OK, 2);
    }

    @Test
    public void testGeneIdNonExisting() {
        testGeneIdHelper(Arrays.asList("ENSG000002972"), HttpStatus.NOT_FOUND, 0);
    }

    @Test
    public void testGendIdsNonExisiting() {
        testGeneIdHelper(Arrays.asList("ENSG00223972", "ENST000450305"), HttpStatus.NOT_FOUND, 0);
    }
}
