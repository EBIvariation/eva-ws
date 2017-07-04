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

import com.github.fakemongo.Fongo;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.mongodb.MongoClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotations;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.Profiles;
import uk.ac.ebi.eva.lib.utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.server.configuration.MongoRepositoryTestConfiguration;

import java.net.URISyntaxException;
import java.util.List;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoRepositoryTestConfiguration.class)
@UsingDataSet(locations = {
        "/test-data/variants.json",
        "/test-data/files.json"
})
@ActiveProfiles(Profiles.TEST_MONGO_FACTORY)
public class RegionWSServerIntegrationTest {

    private static final String TEST_DB = "test-db";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private VariantWithSamplesAndAnnotationsService service;

    @Autowired
    MongoDbFactory mongoDbFactory;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb(TEST_DB);


    @Before
    public void setUp() throws Exception {
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
        List<VariantWithSamplesAndAnnotations> results = regionWsHelper(testRegion);
        assertEquals(expectedVariants, results.size());

        for (VariantWithSamplesAndAnnotations variantEntity : results) {
            assertFalse(variantEntity.getChromosome().isEmpty());
            assertFalse(variantEntity.getReference().isEmpty());
            assertFalse(variantEntity.getAlternate().isEmpty());
            assertNotEquals(0, variantEntity.getStart());
            assertNotEquals(0, variantEntity.getEnd());
        }
    }

    private List<VariantWithSamplesAndAnnotations> regionWsHelper(String testRegion) {
        String url = "/v1/segments/" + testRegion + "/variants?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<VariantWithSamplesAndAnnotations>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantWithSamplesAndAnnotations>>>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<VariantWithSamplesAndAnnotations>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        return queryResponse.getResponse().get(0).getResult();
    }

}
