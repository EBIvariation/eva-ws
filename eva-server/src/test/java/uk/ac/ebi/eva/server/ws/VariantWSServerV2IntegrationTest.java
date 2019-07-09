/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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
import static org.junit.Assert.*;

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
public class VariantWSServerV2IntegrationTest {

    private static final String TEST_DB = "test-db";
    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb(TEST_DB);
    @Autowired
    MongoDbFactory mongoDbFactory;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private VariantWithSamplesAndAnnotationsService service;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void rootTestGetVariantsByVariantCoreString() throws URISyntaxException {
        String url = "/v2/variants/20:60100:A:T?species=mmusculus&assembly=grcm38";
        List<VariantWithSamplesAndAnnotation> variantWithSamplesAndAnnotations = WSTestHelpers.testRestTemplateHelper(
                url, restTemplate);
        assertEquals(1, variantWithSamplesAndAnnotations.size());
        assertTrue(variantWithSamplesAndAnnotations.get(0).getSourceEntries().isEmpty());
        assertNull(variantWithSamplesAndAnnotations.get(0).getAnnotation());
        assertTrue(variantWithSamplesAndAnnotations.get(0).getIds().size() > 0);
    }

    @Test
    public void rootTestGetVariantsByNonExistingVariantCoreString() throws URISyntaxException {
        String url = "/v2/variants/10:0:A:T?species=mmusculus&assembly=grcm38";

        ResponseEntity<QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>>>() {
                });
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(0, response.getBody().getResponse().size());
    }

    @Test
    public void rootTestParameterErrors() throws URISyntaxException {
        String url = "/v2/variants/13:32889669:C:T?species=&assembly=grcm38";
        assertEquals("Please specify a species", testForErrorHelper(url));
    }

    private String testForErrorHelper(String url) {
        return WSTestHelpers.testRestTemplateHelperForError(url, restTemplate);
    }

    @Test
    public void annotationEndPointTestExisting() throws URISyntaxException {
        String url = "/v2/variants/20:60100:A:T/annotations?species=mmusculus&assembly=grcm38";
        ResponseEntity<QueryResponse<QueryResult<Annotation>>> annotations = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<Annotation>>>() {
                });
        assertEquals(HttpStatus.OK, annotations.getStatusCode());
        assertFalse(annotations.getBody().getResponse().get(0).getResult().get(0).getChromosome().isEmpty());
    }

    @Test
    public void annotationEndPointTestForNonExistingAnnotationWithNonExistingVariant() throws URISyntaxException {
        String url = "/v2/variants/100:0:C:T/annotations?species=mmusculus&assembly=grcm38";
        ResponseEntity<QueryResponse<QueryResult<Annotation>>> annotations = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<Annotation>>>() {
                });
        assertEquals(HttpStatus.NOT_FOUND, annotations.getStatusCode());
        assertTrue(annotations.getBody().getResponse().get(0).getResult().size() == 0);
    }

    @Test
    public void annotationEndPointTestForNonExistingAnnotationWithExistingVariant() throws URISyntaxException {
        String url = "/v2/variants/X:1000014:G:A/annotations?species=mmusculus&assembly=grcm38";
        ResponseEntity<QueryResponse<QueryResult<Annotation>>> annotations = restTemplate.exchange(url, HttpMethod.GET,
                null, new ParameterizedTypeReference<QueryResponse<QueryResult<Annotation>>>() {
                });
        assertEquals(HttpStatus.NOT_FOUND, annotations.getStatusCode());
        assertTrue(annotations.getBody().getResponse().get(0).getResult().size() == 0);
    }

    @Test
    public void annotationEndpointTestForError() throws URISyntaxException {
        String url = "/v2/variants/13:32889669:C:T/annotations?species=mmusculus&assembly=grcm38&" +
                "annot-vep-version=1";
        assertEquals("Please specify either both annotation VEP version and annotation VEP cache version, " +
                "or neither", testForErrorHelper(url));
        url = "/v2/variants/13:32889669:C:T/annotations?species=mmusculus&assembly=grcm38&" +
                "annot-vep-cache-version=1";
        assertEquals("Please specify either both annotation VEP version and annotation VEP cache version, " +
                "or neither", testForErrorHelper(url));
    }

    @Test
    public void sourceEntriesEndPointTestExisting() throws URISyntaxException {
        String url = "/v2/variants/20:60100:A:T/sources?species=mmusculus&assembly=grcm38";
        ResponseEntity<QueryResponse<QueryResult<VariantSourceEntryWithSampleNames>>> sources = restTemplate.
                exchange(url, HttpMethod.GET, null,
                        new ParameterizedTypeReference<QueryResponse<QueryResult
                                <VariantSourceEntryWithSampleNames>>>() {
                        });
        assertEquals(HttpStatus.OK, sources.getStatusCode());
        assertFalse(sources.getBody().getResponse().get(0).getResult().get(0).getFileId().isEmpty());
    }

    @Test
    public void sourceEntriesEndPointTestNonExistingWithNonExistingVariant() throws URISyntaxException {
        String url = "/v2/variants/100:0:C:T/sources?species=mmusculus&assembly=grcm38";
        ResponseEntity<QueryResponse<QueryResult<VariantSourceEntryWithSampleNames>>> sources = restTemplate.
                exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<QueryResponse<
                        QueryResult<VariantSourceEntryWithSampleNames>>>() {
                });
        assertEquals(HttpStatus.NOT_FOUND, sources.getStatusCode());
        assertTrue(sources.getBody().getResponse().get(0).getResult().isEmpty());
    }

    @Test
    public void sourceEntriesEndPointTestNonExistingWithExistingVariantAndNonExistingStatistics() throws
            URISyntaxException {
        String url = "/v2/variants/X:1000014:G:A/sources?species=mmusculus&assembly=grcm38";
        ResponseEntity<QueryResponse<QueryResult<VariantSourceEntryWithSampleNames>>> sources = restTemplate.exchange
                (url, HttpMethod.GET, null, new ParameterizedTypeReference<QueryResponse<QueryResult
                        <VariantSourceEntryWithSampleNames>>>() {
                });
        assertEquals(HttpStatus.OK, sources.getStatusCode());
        assertTrue(sources.getBody().getResponse().get(0).getResult().get(0).getCohortStats().isEmpty());
    }

    @Test
    public void sourceEntriesEndpointTestForError() throws URISyntaxException {
        String url = "/v2/variants/13:32889669:C:T/sources?species=mmusculus&assembly=grcm38&" +
                "annot-vep-version=1";
        assertEquals("Please specify either both annotation VEP version and annotation VEP cache version, " +
                "or neither", testForErrorHelper(url));
    }
}
