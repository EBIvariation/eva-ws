/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
import uk.ac.ebi.eva.commons.core.models.ws.VariantSourceEntryWithSampleNames;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.Profiles;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.server.configuration.MongoRepositoryTestConfiguration;

import java.net.URISyntaxException;
import java.util.List;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoRepositoryTestConfiguration.class)
@UsingDataSet(locations = {
        "/test-data/variants.json",
        "/test-data/files.json",
        "/test-data/annotations.json",
        "/test-data/annotation_metadata.json"
})
@ActiveProfiles(Profiles.TEST_MONGO_FACTORY)
public class VariantWSServerIntegrationTest {

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
    public void testGetVariantsByVariantId() throws URISyntaxException {
        testGetVariantsByVariantIdHelper("rs199692280", 1);
    }

    @Test
    public void testGetVariantsByNonExistingVariantId() throws URISyntaxException {
        testGetVariantsByVariantIdHelper("rs1", 0);
    }

    private void testGetVariantsByVariantIdHelper(String testVariantId, int expectedVariants) throws URISyntaxException {
        List<VariantWithSamplesAndAnnotation> results = variantWsHelper(testVariantId);
        assertEquals(expectedVariants, results.size());

        for (VariantWithSamplesAndAnnotation variantEntity : results) {
            assertFalse(variantEntity.getChromosome().isEmpty());
            assertFalse(variantEntity.getReference().isEmpty());
            assertFalse(variantEntity.getAlternate().isEmpty());
            for (VariantSourceEntryWithSampleNames variantSourceEntry : variantEntity.getSourceEntries()) {
                assertFalse(variantSourceEntry.getCohortStats().isEmpty());
            }
            assertNotEquals(0, variantEntity.getStart());
            assertNotEquals(0, variantEntity.getEnd());
        }
    }

    private List<VariantWithSamplesAndAnnotation> variantWsHelper(String testVariantId) {
        String url = "/v1/variants/" + testVariantId + "/info?species=mmusculus_grcm38";
        return testRestTemplateHelper(url);
    }

    @Test
    public void testExcludeSourceEntriesStatistics() {
        String testVariantId = "rs199692280";
        String testExclusion = "sourceEntries.statistics";
        List<VariantWithSamplesAndAnnotation> results = testExcludeHelper(testVariantId, testExclusion);
        for (VariantWithSamplesAndAnnotation variant : results) {
            for (VariantSourceEntryWithSampleNames sourceEntry : variant.getSourceEntries()) {
                assertTrue(sourceEntry.getCohortStats().isEmpty());
            }
        }
    }

    private List<VariantWithSamplesAndAnnotation> testExcludeHelper(String testVariantId, String testExclusion) {
        String url = "/v1/variants/" + testVariantId + "/info?species=mmusculus_grcm38&exclude=" + testExclusion;
        return testRestTemplateHelper(url);
    }

    @Test
    public void testVepVersionAndVepCacheVersionFilter() {
        String testVariantId = "rs199692280";
        String annotationVepVersion = "78";
        String annotationVepCacheversion = "78";
        String url = "/v1/segments/" + testVariantId +
                "/variants?species=mmusculus_grcm38&annot-vep-version=" + annotationVepVersion +
                "&annot-vep-cache-version=" + annotationVepCacheversion;
        List<VariantWithSamplesAndAnnotation> variants = testRestTemplateHelper(url);
        for (VariantWithSamplesAndAnnotation variant : variants) {
            Annotation annotation = variant.getAnnotation();
            assertEquals(annotationVepVersion, annotation.getVepVersion());
            assertEquals(annotationVepCacheversion, annotation.getVepCacheVersion());
        }
    }

    private List<VariantWithSamplesAndAnnotation> testRestTemplateHelper(String url) {
        ResponseEntity<QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>>>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        return queryResponse.getResponse().get(0).getResult();
    }

}
