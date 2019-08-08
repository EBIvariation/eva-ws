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
import uk.ac.ebi.eva.commons.mongodb.entities.projections.VariantStudySummary;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
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
@Import({MongoRepositoryTestConfiguration.class})
@UsingDataSet(locations = {
        "/test-data/files.json"
})
@ActiveProfiles(Profiles.TEST_MONGO_FACTORY)
public class StudyWSServerV2IntegrationTest {

    private static final String TEST_DB = "test-db";

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb(TEST_DB);

    @Autowired
    MongoDbFactory mongoDbFactory;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetStudies() {
        String url = "/v2/studies?species=mmusculus&assembly=grcm38&pageNumber=0&pageSize=1";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Configuration configuration = Configuration.defaultConfiguration()
                .jsonProvider(new JacksonJsonProvider())
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .addOptions(Option.SUPPRESS_EXCEPTIONS);

        List<VariantStudySummary> variantList = JsonPath.using(configuration).parse(response.getBody())
                .read("$['_embedded']['variantStudySummaryList']", new TypeRef<List<VariantStudySummary>>() {
                });

        assertEquals("PRJX00001", variantList.get(0).getStudyId());
        assertEquals("Human Variation Data From dbSNP build 144", variantList.get(0).getStudyName());
        assertEquals(1, variantList.size());

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

    }

    @Test
    public void testInvalidPageRanges() {
        String url = "/v2/studies?species=mmusculus&assembly=grcm38&pageNumber=1000&pageSize=1";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, response.getStatusCode());
        assertEquals("For the given page size, there are 2 page(s), so the correct page range is from 0 to 1" +
                " (both included).", response.getBody());
    }
}