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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.Profiles;
import uk.ac.ebi.eva.server.configuration.MongoRepositoryTestConfiguration;

import java.util.List;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

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
public class IdentifierWSServerV2IntegrationTest {

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

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testForExisting() {
        String url = "/v2/identifiers/rs199692280/variants?species=mmusculus&assembly=grcm38";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Configuration configuration = Configuration.defaultConfiguration()
                .jsonProvider(new JacksonJsonProvider())
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .addOptions(Option.SUPPRESS_EXCEPTIONS);
        List<Variant> variantList = JsonPath.using(configuration).parse(response.getBody())
                .read("$['_embedded']['variantList']", new TypeRef<List<Variant>>() {
                });
        assertFalse(JsonPath.using(configuration).parse(response.getBody()).read
                ("$['_embedded']['variantList'][0]['_links']['sources']['href']", new TypeRef<String>() {
                }).isEmpty());
        assertFalse(JsonPath.using(configuration).parse(response.getBody()).read
                ("$['_embedded']['variantList'][0]['_links']['annotation']['href']", new TypeRef<String>() {
                }).isEmpty());

        assertTrue(variantList.size() > 0);
        Variant variant = variantList.get(0);
        assertEquals("20", variant.getChromosome());
        assertEquals("T", variant.getAlternate());
        assertEquals("A", variant.getReference());
        assertEquals(60100L, variant.getStart());
        assertEquals(60100L, variant.getEnd());
    }

    @Test
    public void testForNonExisting() {
        String url = "/v2/identifiers/abcd/variants?species=hsapiens&assembly=grch37";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}
