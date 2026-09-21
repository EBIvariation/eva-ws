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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.eva.commons.core.models.Annotation;
import uk.ac.ebi.eva.commons.core.models.ws.VariantSourceEntryWithSampleNames;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.Profiles;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.server.configuration.MongoRepositoryTestConfiguration;
import uk.ac.ebi.eva.server.utils.MongoTestContainerHelper;
import uk.ac.ebi.eva.server.utils.MongoTestDataLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoRepositoryTestConfiguration.class)
@ActiveProfiles(Profiles.TEST_MONGO_FACTORY)
public class VariantWSServerIntegrationTest extends MongoTestContainerHelper {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private VariantWithSamplesAndAnnotationsService service;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    @BeforeEach
    public void setUp() throws Exception {
        mongoTemplate.getDb().drop();

        MongoTestDataLoader mongoTestDataLoader = new MongoTestDataLoader(mongoTemplate, resourceLoader);
        mongoTestDataLoader.load("/test-data/variants.json");
        mongoTestDataLoader.load("/test-data/files.json");
        mongoTestDataLoader.load("/test-data/annotations.json");
        mongoTestDataLoader.load("/test-data/annotation_metadata.json");
    }

    @AfterEach
    public void tearDown() {
        mongoTemplate.getDb().drop();
    }

    @Test
    public void testGetVariantsByVariantId() {
        testGetVariantsByVariantIdHelper("rs199692280", 1);
    }

    @Test
    public void testGetVariantsByNonExistingVariantId() {
        testGetVariantsByVariantIdHelper("rs1", 0);
    }

    private void testGetVariantsByVariantIdHelper(String testVariantId, int expectedVariants) {
        List<VariantWithSamplesAndAnnotation> results = variantWsHelper(testVariantId);
        WSTestHelpers.checkVariantsInFullResults(results, expectedVariants);
    }

    private List<VariantWithSamplesAndAnnotation> variantWsHelper(String testVariantId) {
        String url = "/v1/variants/" + testVariantId + "/info?species=mmusculus_grcm38";
        return WSTestHelpers.testRestTemplateHelper(url, restTemplate);
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
        return WSTestHelpers.testRestTemplateHelper(url, restTemplate);
    }

    @Test
    public void testVepVersionAndVepCacheVersionFilter() {
        String testVariantId = "rs199692280";
        String annotationVepVersion = "78";
        String annotationVepCacheversion = "78";
        String url = "/v1/variants/" + testVariantId +
                "/info?species=mmusculus_grcm38&annot-vep-version=" + annotationVepVersion +
                "&annot-vep-cache-version=" + annotationVepCacheversion;
        List<VariantWithSamplesAndAnnotation> variants = WSTestHelpers.testRestTemplateHelper(url, restTemplate);
        for (VariantWithSamplesAndAnnotation variant : variants) {
            Annotation annotation = variant.getAnnotation();
            assertEquals(annotationVepVersion, annotation.getVepVersion());
            assertEquals(annotationVepCacheversion, annotation.getVepCacheVersion());
        }
        assertTrue(variants.size() > 0);
    }

    @Test
    public void testVariantSearchByList() throws Exception {
        String testVariantIds = "rs370478,rs199692280";
        String url = "/v1/variants/" + testVariantIds + "?species=mmusculus_grcm38";
        JSONObject jsonObject = WSTestHelpers.testRestTemplateHelperJsonObject(url, restTemplate);
        JSONArray responseArray = jsonObject.getJSONArray("response");
        for (int i = 0; i < responseArray.length(); ++i) {
            JSONObject response = responseArray.getJSONObject(i);
            JSONArray resultArray = response.getJSONArray("result");
            for (int j = 0; j < resultArray.length(); ++j) {
                JSONObject result = resultArray.getJSONObject(j);
                JSONArray consequenceTypes = result.getJSONObject("annotation").getJSONArray("consequenceTypes");
                for (int k = 0; k < consequenceTypes.length(); ++k) {
                    JSONObject consequenceType = consequenceTypes.getJSONObject(k);
                    if (!consequenceType.has("ensemblTranscriptId")) {
                        continue;
                    }
                    if (consequenceType.getString("ensemblTranscriptId").equals("ENST00000426146")) {
                        assertTrue(consequenceType.has("proteinSubstitutionScores"));
                        assertFalse(consequenceType.has("sift"));
                        assertFalse(consequenceType.has("polyphen"));
                    }
                }
            }
        }
    }

    @Test
    public void testProteinSubstitutionScoresModel() throws Exception {
        String testVariantId = "rs370478";
        String url = "/v1/variants/" + testVariantId + "/info?species=mmusculus_grcm38";
        JSONObject jsonObject = WSTestHelpers.testRestTemplateHelperJsonObject(url, restTemplate);
        JSONArray responseArray = jsonObject.getJSONArray("response");
        for (int i = 0; i < responseArray.length(); ++i) {
            JSONObject response = responseArray.getJSONObject(i);
            JSONArray resultArray = response.getJSONArray("result");
            for (int j = 0; j < resultArray.length(); ++j) {
                JSONObject result = resultArray.getJSONObject(j);
                JSONArray consequenceTypes = result.getJSONObject("annotation").getJSONArray("consequenceTypes");
                for (int k = 0; k < consequenceTypes.length(); ++k) {
                    JSONObject consequenceType = consequenceTypes.getJSONObject(k);
                    if (!consequenceType.has("ensemblTranscriptId")) {
                        continue;
                    }
                    if (consequenceType.getString("ensemblTranscriptId").equals("ENST00000426146")) {
                        assertTrue(consequenceType.has("proteinSubstitutionScores"));
                        assertFalse(consequenceType.has("sift"));
                        assertFalse(consequenceType.has("polyphen"));
                    }
                }
            }
        }
    }

    @Test
    public void testCountVariants() {
        Long expectedNumberOfVariants = new Long(5);

        String url = "/v1/variants/count";
        ResponseEntity<QueryResponse<QueryResult<Long>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<Long>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<Long>> queryResponse = response.getBody();
        assertEquals(expectedNumberOfVariants, queryResponse.getResponse().get(0).getResult().get(0));
    }
}
