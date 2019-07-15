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

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.core.models.Annotation;
import uk.ac.ebi.eva.commons.core.models.FeatureCoordinates;
import uk.ac.ebi.eva.commons.core.models.ws.VariantSourceEntryWithSampleNames;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.Profiles;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.server.configuration.MongoRepositoryTestConfiguration;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoRepositoryTestConfiguration.class)
@UsingDataSet(locations = {
        "/test-data/features.json"
})
@ActiveProfiles(Profiles.TEST_MONGO_FACTORY)
public class GeneWSServerV2IntegrationTest {

    private static final String TEST_DB = "test-db";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    MongoDbFactory mongoDbFactory;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb(TEST_DB);

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGeneIdExisint() {
        testGeneIdHelper(Arrays.asList("ENSG00000223972"), HttpStatus.OK, 1);
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
        testGeneIdHelper(Arrays.asList("ENSG00000223972", "ENST00000450305"), HttpStatus.OK, 2);
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
