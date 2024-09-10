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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
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
import uk.ac.ebi.eva.lib.utils.TaxonomyUtils;
import uk.ac.ebi.eva.server.configuration.MongoRepositoryTestConfiguration;
import uk.ac.ebi.eva.server.ws.contigalias.ContigAliasService;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContigAliasService contigAliasService;

    @MockBean
    private TaxonomyUtils taxonomyUtils;

    @Before
    public void setUp() throws Exception {
        given(contigAliasService.translateContigFromInsdc("13", null))
                .willReturn("");
        given(contigAliasService.translateContigFromInsdc("20", null))
                .willReturn("");
        given(contigAliasService.translateContigToInsdc("10", "grcm38", null))
                .willReturn("10");
        given(contigAliasService.translateContigToInsdc("100", "grcm38", null))
                .willReturn("100");
        given(taxonomyUtils.getAssemblyAccessionForAssemblyCode("grcm38")).willReturn(Optional.empty());
    }

    @Test
    public void rootTestGetVariantsByVariantCoreString() throws URISyntaxException {
        String url = "/v2/variants/20:60100:A:T?species=mmusculus&assembly=grcm38";
        VariantWithSamplesAndAnnotation variantWithSamplesAndAnnotations = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<VariantWithSamplesAndAnnotation>() {
                }).getBody();
        assertTrue(variantWithSamplesAndAnnotations.getSourceEntries().isEmpty());
        assertNull(variantWithSamplesAndAnnotations.getAnnotation());
        assertTrue(variantWithSamplesAndAnnotations.getIds().size() > 0);
    }

    @Test
    public void rootTestGetVariantsByNonExistingVariantCoreString() throws URISyntaxException {
        String url = "/v2/variants/10:0:A:T?species=mmusculus&assembly=grcm38";
        testForNonExistingHelper(url);
    }

    private void testForNonExistingHelper(String url) throws URISyntaxException {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
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
        Annotation translatedAnnotation = new Annotation("chr1", 0, 0, null, null, null, null);
        given(contigAliasService.getAnnotationWithTranslatedContig(any(), eq(null)))
                .willReturn(translatedAnnotation);
        String url = "/v2/variants/20:60100:A:T/annotations?species=mmusculus&assembly=grcm38";
        ResponseEntity<Annotation> annotations = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Annotation>() {
                });
        assertEquals(HttpStatus.OK, annotations.getStatusCode());
        assertFalse(annotations.getBody().getChromosome().isEmpty());
    }

    @Test
    public void annotationEndPointTestForNonExistingAnnotationWithNonExistingVariant() throws URISyntaxException {
        String url = "/v2/variants/100:0:C:T/annotations?species=mmusculus&assembly=grcm38";
        testForNonExistingHelper(url);
    }

    @Test
    public void annotationEndPointTestForNonExistingAnnotationWithExistingVariant() throws URISyntaxException {
        String url = "/v2/variants/X:1000014:G:A/annotations?species=mmusculus&assembly=grcm38";
        testForNonExistingHelper(url);
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
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        Configuration configuration = Configuration.defaultConfiguration()
                .jsonProvider(new JacksonJsonProvider())
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .addOptions(Option.SUPPRESS_EXCEPTIONS);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<VariantSourceEntryWithSampleNames> sources = JsonPath.using(configuration).parse(response.getBody())
                .read("$['_embedded']['variantSourceEntryWithSampleNamesList']", new TypeRef
                        <List<VariantSourceEntryWithSampleNames>>() {
                });
        assertFalse(sources.get(0).getFileId().isEmpty());
    }

    @Test
    public void sourceEntriesEndPointTestNonExistingWithNonExistingVariant() throws URISyntaxException {
        String url = "/v2/variants/100:0:C:T/sources?species=mmusculus&assembly=grcm38";
        testForNonExistingHelper(url);
    }

    @Test
    public void sourceEntriesEndPointTestNonExistingWithExistingVariantAndNonExistingStatistics() throws
            URISyntaxException {
        String url = "/v2/variants/X:1000014:G:A/sources?species=mmusculus&assembly=grcm38";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        Configuration configuration = Configuration.defaultConfiguration()
                .jsonProvider(new JacksonJsonProvider())
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .addOptions(Option.SUPPRESS_EXCEPTIONS);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<VariantSourceEntryWithSampleNames> sources = JsonPath.using(configuration).parse(response.getBody())
                .read("$['_embedded']['variantSourceEntryWithSampleNamesList']", new TypeRef
                        <List<VariantSourceEntryWithSampleNames>>() {
                });
        assertTrue(sources.get(0).getCohortStats().isEmpty());
    }

    @Test
    public void sourceEntriesEndpointTestForError() throws URISyntaxException {
        String url = "/v2/variants/13:32889669:C:T/sources?species=mmusculus&assembly=grcm38&" +
                "annot-vep-version=1";
        assertEquals("Please specify either both annotation VEP version and annotation VEP cache version, " +
                "or neither", testForErrorHelper(url));
    }

    @Test
    public void rootTestForDeletions() throws URISyntaxException {
        String url = "/v2/variants/13:32889711:T:?species=mmusculus&assembly=grcm38";
        VariantWithSamplesAndAnnotation variantWithSamplesAndAnnotations = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<VariantWithSamplesAndAnnotation>() {
                }).getBody();
        assertEquals("13", variantWithSamplesAndAnnotations.getChromosome());
        assertEquals("T", variantWithSamplesAndAnnotations.getReference());
        assertTrue(variantWithSamplesAndAnnotations.getAlternate().isEmpty());
        assertEquals(32889711, variantWithSamplesAndAnnotations.getStart());
    }

    @Test
    public void rootTestForInsertions() throws URISyntaxException {
        String url = "/v2/variants/13:32889711::A?species=mmusculus&assembly=grcm38";
        VariantWithSamplesAndAnnotation variantWithSamplesAndAnnotations = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<VariantWithSamplesAndAnnotation>() {
                }).getBody();
        assertEquals("13", variantWithSamplesAndAnnotations.getChromosome());
        assertTrue(variantWithSamplesAndAnnotations.getReference().isEmpty());
        assertEquals("A", variantWithSamplesAndAnnotations.getAlternate());
        assertEquals(32889711, variantWithSamplesAndAnnotations.getStart());
    }

}
