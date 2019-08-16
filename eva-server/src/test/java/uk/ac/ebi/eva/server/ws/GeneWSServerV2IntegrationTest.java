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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.lib.Profiles;
import uk.ac.ebi.eva.server.configuration.MongoRepositoryTestConfiguration;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoRepositoryTestConfiguration.class)
@UsingDataSet(locations = {
        "/test-data/features.json",
        "/test-data/variants.json",
        "/test-data/files.json",
        "/test-data/annotations.json",
        "/test-data/annotation_metadata.json"
})
@ActiveProfiles(Profiles.TEST_MONGO_FACTORY)
public class GeneWSServerV2IntegrationTest {

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
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetVariantsByExistingGene() throws URISyntaxException {
        Variant variant = testGetVariantsGeneHelper("ENSG00000227232", 1, HttpStatus.OK).get(0);
        assertEquals("20", variant.getChromosome());
        assertEquals("A", variant.getReference());
        assertEquals("T", variant.getAlternate());
    }

    private List<Variant> testGetVariantsGeneHelper(String testRegion, int expectedVariants, HttpStatus status)
            throws URISyntaxException {
        String url = "/v2/genes/" + testRegion + "/variants?species=mmusculus&assembly=grcm38";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(status, response.getStatusCode());
        if (status == HttpStatus.NO_CONTENT) {
            assertNull(response.getBody());
            return null;
        }
        Configuration configuration = Configuration.defaultConfiguration()
                .jsonProvider(new JacksonJsonProvider())
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .addOptions(Option.SUPPRESS_EXCEPTIONS);
        List<Variant> variantList = JsonPath.using(configuration).parse(response.getBody())
                .read("$['_embedded']['variantList']", new TypeRef<List<Variant>>() {
                });

        if (variantList == null) {
            assertEquals(0, expectedVariants);
            return null;
        }
        assertEquals(expectedVariants, variantList.size());
        return variantList;
    }

    @Test
    public void testGetVariantsByExistingGenes() throws URISyntaxException {
        List<Variant> variants = testGetVariantsGeneHelper("ENSG00000227232,ENST00000488147", 2, HttpStatus.OK);
        assertEquals("20", variants.get(0).getChromosome());
        assertEquals("A", variants.get(0).getReference());
        assertEquals("T", variants.get(0).getAlternate());
        assertEquals("20", variants.get(1).getChromosome());
        assertEquals("C", variants.get(1).getReference());
        assertEquals("G", variants.get(1).getAlternate());
    }

    @Test
    public void testGetVariantsByNonExistingGene() throws URISyntaxException {
        testGetVariantsGeneHelper("nonexisting", 0, HttpStatus.NO_CONTENT);
    }

    @Test
    public void testGetVariantsByNonExistingGenes() throws URISyntaxException {
        testGetVariantsGeneHelper("nonexisting,nonexisting", 0, HttpStatus.NO_CONTENT);
    }

    @Test
    public void testPagination() {
        String url = "/v2/genes/ENSG00000227232,ENST00000488147/variants?species=mmusculus&assembly=grcm38&pageSize=1";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Configuration configuration = Configuration.defaultConfiguration()
                .jsonProvider(new JacksonJsonProvider())
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .addOptions(Option.SUPPRESS_EXCEPTIONS);

        Integer totalNumberOfElements = JsonPath.using(configuration).parse(response.getBody())
                .read("$['page']['totalElements']", Integer.class);
        Integer pageNumber = JsonPath.using(configuration).parse(response.getBody())
                .read("$['page']['number']", Integer.class);
        Integer size = JsonPath.using(configuration).parse(response.getBody())
                .read("$['page']['size']", Integer.class);
        Integer totalPages = JsonPath.using(configuration).parse(response.getBody())
                .read("$['page']['totalPages']", Integer.class);

        assertEquals(2, totalNumberOfElements.intValue());
        assertEquals(0, pageNumber.intValue());
        assertEquals(1, size.intValue());
        assertEquals(2, totalPages.intValue());

        url = "/v2/genes/ENSG00000227232,ENST00000488147/variants?species=mmusculus&assembly=grcm38&pageSize=1&pageNumber=1";
        response = restTemplate.getForEntity(url, String.class);
    }

    @Test
    public void testInvalidPageRanges() {
        String url = "/v2/genes/ENSG00000227232/variants?species=mmusculus&assembly=grcm38?&pageNumber=1000&" +
                "pageSize=1";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, response.getStatusCode());
        assertEquals("For the given page size, there are 1 page(s), so the correct page range is from 0 to 0" +
                " (both included).", response.getBody());
    }

    @Test
    public void testBufferValue() {
        String url = "/v2/genes/ENSG00000227232/variants?species=mmusculus&assembly=grcm38&buffer=10000";
        assertEquals(2, testbufferValueParameterHelper(url));

        url = "/v2/genes/ENSG00000227232/variants?species=mmusculus&assembly=grcm38&buffer=0";
        assertEquals(1, testbufferValueParameterHelper(url));
    }

    private int testbufferValueParameterHelper(String url) {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Configuration configuration = Configuration.defaultConfiguration()
                .jsonProvider(new JacksonJsonProvider())
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .addOptions(Option.SUPPRESS_EXCEPTIONS);
        List<Variant> variantList = JsonPath.using(configuration).parse(response.getBody())
                .read("$['_embedded']['variantList']", new TypeRef<List<Variant>>() {
                });
        return variantList.size();
    }
}
