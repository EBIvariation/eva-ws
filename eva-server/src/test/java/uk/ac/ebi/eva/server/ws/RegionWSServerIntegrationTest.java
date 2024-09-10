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

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.eva.commons.core.models.Annotation;
import uk.ac.ebi.eva.commons.core.models.ws.VariantSourceEntryWithSampleNames;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.Profiles;
import uk.ac.ebi.eva.lib.utils.TaxonomyUtils;
import uk.ac.ebi.eva.server.configuration.MongoRepositoryTestConfiguration;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

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
public class RegionWSServerIntegrationTest {

    private static final String TEST_DB = "test-db";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private VariantWithSamplesAndAnnotationsService service;

    @MockBean
    private TaxonomyUtils taxonomyUtils;

    @Autowired
    MongoDbFactory mongoDbFactory;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb(TEST_DB);

    @Before
    public void setUp() throws Exception {
        given(taxonomyUtils.getAssemblyAccessionForAssemblyCode("grcm38")).willReturn(Optional.empty());
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
        List<VariantWithSamplesAndAnnotation> results = regionWsHelper(testRegion);
        WSTestHelpers.checkVariantsInFullResults(results, expectedVariants);
    }

    private List<VariantWithSamplesAndAnnotation> regionWsHelper(String testRegion) {
        String url = "/v1/segments/" + testRegion + "/variants?species=mmusculus_grcm38";
        return WSTestHelpers.testRestTemplateHelper(url, restTemplate);
    }

    @Test
    public void testExcludeSourceEntriesStatistics() {
        String testRegion = "20:60099-60102";
        String testExclusion = "sourceEntries.statistics";
        List<VariantWithSamplesAndAnnotation> results = testExcludeHelper(testRegion, testExclusion);
        for (VariantWithSamplesAndAnnotation variant : results) {
            for (VariantSourceEntryWithSampleNames sourceEntry : variant.getSourceEntries()) {
                assertTrue(sourceEntry.getCohortStats().isEmpty());
            }
        }
    }

    private List<VariantWithSamplesAndAnnotation> testExcludeHelper(String testRegion, String testExclusion) {
        String url = "/v1/segments/" + testRegion + "/variants?species=mmusculus_grcm38&exclude=" + testExclusion;
        return WSTestHelpers.testRestTemplateHelper(url, restTemplate);
    }

    @Test
    public void testVepVersionAndVepCacheVersionFilter() {
        String testRegion = "20:60000-62000";
        String annotationVepVersion = "78";
        String annotationVepCacheversion = "78";
        String url = "/v1/segments/" + testRegion +
                "/variants?species=mmusculus_grcm38&annot-vep-version=" + annotationVepVersion +
                "&annot-vep-cache-version=" + annotationVepCacheversion;
        List<VariantWithSamplesAndAnnotation> variants = WSTestHelpers.testRestTemplateHelper(url, restTemplate);
        for (VariantWithSamplesAndAnnotation variant : variants) {
            Annotation annotation = variant.getAnnotation();
            assertEquals(annotationVepVersion, annotation.getVepVersion());
            assertEquals(annotationVepCacheversion, annotation.getVepCacheVersion());
        }
    }

    @Test
    public void testIllegalLimitParameter() {
        String testRegion = "20:60000-80000";
        String annotationVepVersion = "78";
        String annotationVepCacheversion = "78";
        int limit = 100000;
        String url = UriComponentsBuilder.fromUriString("")
                .path("/v1/segments/")
                .path(testRegion)
                .path("/variants")
                .queryParam("species", "mmusculus_grcm38")
                .queryParam("annot-vep-version", annotationVepVersion)
                .queryParam("annot-vep-cache-version", annotationVepCacheversion)
                .queryParam("limit", limit)
                .build().toString();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
