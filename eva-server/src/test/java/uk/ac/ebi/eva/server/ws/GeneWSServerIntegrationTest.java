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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.eva.commons.core.models.Annotation;
import uk.ac.ebi.eva.commons.core.models.ws.VariantSourceEntryWithSampleNames;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.Profiles;
import uk.ac.ebi.eva.server.configuration.MongoRepositoryTestConfiguration;
import uk.ac.ebi.eva.server.utils.MongoTestContainerHelper;
import uk.ac.ebi.eva.server.utils.MongoTestDataLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoRepositoryTestConfiguration.class)
@ActiveProfiles(Profiles.TEST_MONGO_FACTORY)
public class GeneWSServerIntegrationTest extends MongoTestContainerHelper {

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
    public void testGetVariantsByGene() {
        testGetVariantsByGeneHelper("SH3YL1", 1);
    }

    @Test
    public void testGetVariantsByGenes() {
        testGetVariantsByGeneHelper("SH3YL1,DDX11L5", 2);
    }

    @Test
    public void testGetVariantsByNonExistingGene() {
        testGetVariantsByGeneHelper("ABC", 0);
    }

    private void testGetVariantsByGeneHelper(String testGene, int expectedVariants) {
        List<VariantWithSamplesAndAnnotation> results = geneWsHelper(testGene);
        WSTestHelpers.checkVariantsInFullResults(results, expectedVariants);
    }

    private List<VariantWithSamplesAndAnnotation> geneWsHelper(String testGene) {
        String url = "/v1/genes/" + testGene + "/variants?species=mmusculus_grcm38";
        return WSTestHelpers.testRestTemplateHelper(url, restTemplate);
    }

    @Test
    public void testExcludeSourceEntries() {
        String testGene = "SH3YL1";
        String testExclusion = "sourceEntries";
        List<VariantWithSamplesAndAnnotation> results = testExcludeHelper(testGene, testExclusion);
        for (VariantWithSamplesAndAnnotation variant : results) {
            for (VariantSourceEntryWithSampleNames sourceEntry : variant.getSourceEntries()) {
                assertTrue(sourceEntry.getCohortStats().isEmpty());
            }
        }
    }

    private List<VariantWithSamplesAndAnnotation> testExcludeHelper(String testGene, String testExclusion) {
        String url = "/v1/genes/" + testGene + "/variants?species=mmusculus_grcm38&exclude=" + testExclusion;
        return WSTestHelpers.testRestTemplateHelper(url, restTemplate);
    }

    @Test
    public void testVepVersionAndVepCacheVersionFilter() {
        String testGene = "DDX11L5";
        String annotationVepVersion = "78";
        String annotationVepCacheversion = "78";
        String url = "/v1/genes/" + testGene +
                "/variants?species=mmusculus_grcm38&annot-vep-version=" + annotationVepVersion +
                "&annot-vep-cache-version=" + annotationVepCacheversion;
        List<VariantWithSamplesAndAnnotation> variants = WSTestHelpers.testRestTemplateHelper(url, restTemplate);
        for (VariantWithSamplesAndAnnotation variant : variants) {
            Annotation annotation = variant.getAnnotation();
            assertEquals(annotationVepVersion, annotation.getVepVersion());
            assertEquals(annotationVepCacheversion, annotation.getVepCacheVersion());
        }
    }

}
