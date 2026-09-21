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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.eva.commons.mongodb.entities.projections.VariantStudySummary;
import uk.ac.ebi.eva.lib.Profiles;
import uk.ac.ebi.eva.server.configuration.MongoRepositoryTestConfiguration;
import uk.ac.ebi.eva.server.utils.MongoTestContainerHelper;
import uk.ac.ebi.eva.server.utils.MongoTestDataLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({MongoRepositoryTestConfiguration.class})
@ActiveProfiles(Profiles.TEST_MONGO_FACTORY)
public class StudyWSServerV2IntegrationTest extends MongoTestContainerHelper {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    @BeforeEach
    public void setUp() throws Exception {
        mongoTemplate.getDb().drop();

        MongoTestDataLoader mongoTestDataLoader = new MongoTestDataLoader(mongoTemplate, resourceLoader);
        mongoTestDataLoader.load("/test-data/files.json");
    }

    @AfterEach
    public void tearDown() {
        mongoTemplate.getDb().drop();
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

        // Order of elements is not guaranteed, so either study could be returned first
        String firstStudyId = variantList.get(0).getStudyId();
        assert (firstStudyId.equals("PRJX00001") || firstStudyId.equals("PRJEB5829"));
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
        assertEquals(2, totalPages.intValue());
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