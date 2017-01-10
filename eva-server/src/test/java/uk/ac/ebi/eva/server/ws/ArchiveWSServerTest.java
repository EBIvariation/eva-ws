/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2015 EMBL - European Bioinformatics Institute
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

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.lib.metadata.ArchiveDgvaDBAdaptor;
import uk.ac.ebi.eva.lib.metadata.ArchiveEvaproDBAdaptor;
import uk.ac.ebi.eva.lib.metadata.StudyDgvaDBAdaptor;
import uk.ac.ebi.eva.lib.metadata.StudyEvaproDBAdaptor;
import uk.ac.ebi.eva.lib.models.Assembly;
import uk.ac.ebi.eva.lib.models.VariantStudy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.unregisterParser;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArchiveWSServerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ArchiveEvaproDBAdaptor archiveEvaproDBAdaptor;

    @MockBean
    private StudyEvaproDBAdaptor studyEvaproDBAdaptor;

    @MockBean
    private StudyDgvaDBAdaptor studyDgvaDBAdaptor;

    @Before
    public void setup() throws URISyntaxException {
        // species test data
        Assembly grch37 = new Assembly("GCA_000001405.1", "GCA_000001405", "1", "GRCh37", "grc3h7", 9606, "Human", "Homo Sapiens", "hsapiens", "human");

        Assembly grch38 = new Assembly("GCA_000001405.18", "GCA_000001405", "18", "GRCh38.p3", "grc3h8", 9606, "Human",
                              "Homo Sapiens", "hsapiens", "human");
        Assembly umd31 = new Assembly("GCA_000003055.3", "GCA_000003055", "3", "Bos_taurus_UMD_3.1", "umd31", 9913, "Cattle",
                             "Bos taurus", "btaurus", "cow");
        BDDMockito.given(this.archiveEvaproDBAdaptor.getSpecies(anyString(), eq(true))).willReturn(encapsulateInQueryResult(grch37, grch38, umd31));
        BDDMockito.given(this.archiveEvaproDBAdaptor.countSpecies()).willReturn(encapsulateInQueryResult(3L));

        BDDMockito.given(this.archiveEvaproDBAdaptor.countFiles()).willReturn(encapsulateInQueryResult(5L));

        BDDMockito.given(this.archiveEvaproDBAdaptor.countStudies()).willReturn(encapsulateInQueryResult(3L));


        VariantStudy study1 = new VariantStudy("Human Test study 1", "S1", null, "Study 1 description", new int[]{9606},
                                               "Human", "Homo Sapiens", "Germline", "EBI", "DNA", "multi-isolate",
                                               VariantStudy.StudyType.CASE_CONTROL, "Exome Sequencing", "ES", "GRCh37",
                                               "Illumina", new URI("http://www.s1.org"), new String[]{"10"}, 1000, 10);
        VariantStudy study2 = new VariantStudy("Human Test study 2", "S2", null, "Study 2 description", new int[]{9606},
                                               "Human", "Homo Sapiens", "Germline", "EBI", "DNA", "multi-isolate",
                                               VariantStudy.StudyType.AGGREGATE, "Exome Sequencing", "ES", "GRCh38",
                                               "Illumina", new URI("http://www.s2.org"), new String[]{"13"}, 5000, 4);
        VariantStudy study3 = new VariantStudy("Cow Test study 1", "CS1", null, "Cow study 1 description",
                                               new int[]{9913}, "Cow", "Bos taurus", "Germline", "EBI", "DNA",
                                               "multi-isolate", VariantStudy.StudyType.AGGREGATE,
                                               "Whole Genome Sequencing", "WGSS", "Bos_taurus_UMD_3.1", "Illumina",
                                               new URI("http://www.cs1.org"), new String[]{"1", "2"}, 1300, 12);
        BDDMockito.given(studyEvaproDBAdaptor.getAllStudies(anyObject()))
                  .willReturn(encapsulateInQueryResult(study1, study2, study3));

        VariantStudy svStudy1 = new VariantStudy("Human SV Test study 1", "svS1", null, "SV study 1 description",
                                                 new int[]{9606},
                                                 "Human", "Homo Sapiens", "Germline", "EBI", "DNA", "multi-isolate",
                                                 VariantStudy.StudyType.CASE_CONTROL, "Exome Sequencing", "ES",
                                                 "GRCh37", "Illumina", new URI("http://www.s1.org"), new String[]{"10"},
                                                 1000, 10);
        VariantStudy svStudy2 = new VariantStudy("Human SVV Test study 2", "svS2", null, "SV study 2 description",
                                                 new int[]{9606}, "Human", "Homo Sapiens", "Germline", "EBI", "DNA",
                                                 "multi-isolate", VariantStudy.StudyType.AGGREGATE, "Exome Sequencing",
                                                 "ES", "GRCh38", "Illumina", new URI("http://www.s2.org"),
                                                 new String[]{"13"}, 5000, 4);
        VariantStudy svStudy3 = new VariantStudy("Cow SV Test study 1", "svCS1", null, "SV cow study 1 description",
                                               new int[]{9913}, "Cow", "Bos taurus", "Germline", "EBI", "DNA",
                                               "multi-isolate", VariantStudy.StudyType.AGGREGATE,
                                               "Whole Genome Sequencing", "WGSS", "Bos_taurus_UMD_3.1", "Illumina",
                                               new URI("http://www.cs1.org"), new String[]{"1", "2"}, 1300, 12);
        BDDMockito.given(studyDgvaDBAdaptor.getAllStudies(anyObject()))
                  .willReturn(encapsulateInQueryResult(svStudy1, svStudy2, svStudy3));
        // TODO: why BDDMockito.given instead of when?

    }

    private QueryResult encapsulateInQueryResult(Object... results) {
        return new QueryResult(null, 0, results.length, results.length, null, null, Arrays.asList(results));
    }


    @Test
    public void testCountFiles() throws URISyntaxException {
        ResponseEntity<QueryResponse> response = this.restTemplate.getForEntity("/v1/meta/files/count", QueryResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List queryResponse = response.getBody().getResponse();
        assertEquals(1, queryResponse.size());

        List results = (List)((Map)queryResponse.get(0)).get("result");
        assertEquals(1, results.size());
        assertTrue((Integer)results.get(0) >= 0);
    }

    @Test
    public void testCountSpecies() throws URISyntaxException {
        ResponseEntity<QueryResponse> response = this.restTemplate.getForEntity("/v1/meta/species/count", QueryResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List queryResponse = response.getBody().getResponse();
        assertEquals(1, queryResponse.size());

        List results = (List)((Map)queryResponse.get(0)).get("result");
        assertEquals(1, results.size());
        assertEquals(3, results.get(0));
    }

    @Test
    public void testGetSpecies() throws URISyntaxException {
        ResponseEntity<QueryResponse> response = this.restTemplate.getForEntity("/v1/meta/species/list", QueryResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List queryResponse = response.getBody().getResponse();
        assertEquals(1, queryResponse.size());

        List<Map> result = (List<Map>)((Map)queryResponse.get(0)).get("result");
        assertTrue(result.size() >= 1);

        for (Map m : result) {
            String missingField = String.format("%s required field missing", m.get("assemblyName"));

            if (m.containsKey("assemblyChain")) { // Accessioned assembly
                assertTrue(missingField, m.containsKey("assemblyAccession"));
                assertTrue(missingField, m.containsKey("assemblyChain"));
                assertTrue(missingField, m.containsKey("assemblyCode"));
                assertTrue(missingField, m.containsKey("assemblyName"));
                assertTrue(missingField, m.containsKey("assemblyVersion"));
            } else { // Non-accessioned assembly
                assertTrue(missingField, m.containsKey("assemblyCode"));
                assertTrue(missingField, m.containsKey("assemblyName"));
            }

            assertTrue(missingField, m.containsKey("taxonomyId"));
            assertTrue(missingField, m.containsKey("taxonomyCode"));
//            assertTrue(missingField, m.containsKey("taxonomyCommonName")); // This is not really mandatory
            assertTrue(missingField, m.containsKey("taxonomyEvaName"));
            assertTrue(missingField, m.containsKey("taxonomyScientificName"));
        }
    }

    @Test
    public void testCountStudies() throws URISyntaxException {
        ResponseEntity<QueryResponse> response = this.restTemplate.getForEntity("/v1/meta/studies/count", QueryResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List queryResponse = response.getBody().getResponse();
        assertEquals(1, queryResponse.size());

        List<Integer> result = (List)((Map)queryResponse.get(0)).get("result");
        assertEquals(1, result.size());
        assertTrue(result.get(0) >= 0);
    }

    @Test
    public void testGetBrowsableStudiesNoSpecies() throws URISyntaxException {
        ResponseEntity<QueryResponse> response = this.restTemplate.getForEntity("/v1/meta/studies/list", QueryResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetBrowsableStudiesBySpecies() throws URISyntaxException {
        ResponseEntity<QueryResponse> response = this.restTemplate.getForEntity("/v1/meta/studies/list?species=hsapiens_grch37", QueryResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List queryResponse = response.getBody().getResponse();
        assertEquals(1, queryResponse.size());

        List<Map> result = (List<Map>)((Map)queryResponse.get(0)).get("result");
        assertTrue(result.size() >= 1);
    }

    @Test
    public void testGetStudies() throws URISyntaxException {
        ResponseEntity<QueryResponse> response = this.restTemplate.getForEntity("/v1/meta/studies/all", QueryResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List queryResponse = response.getBody().getResponse();
        assertEquals(1, queryResponse.size());

        List<Map> result = (List<Map>)((Map)queryResponse.get(0)).get("result");
        assertTrue(result.size() >= 1);

        for (Map m : result) {
            String missingField = String.format("%s required field missing", m.get("name"));

            assertTrue(missingField, m.containsKey("name"));
            assertTrue(missingField, m.containsKey("id"));
            assertTrue(missingField, m.containsKey("description"));

            assertTrue(missingField, m.containsKey("taxonomyId"));
            List<Integer> taxonomyIds = (List<Integer>)m.get("taxonomyId");
//            List<Integer> taxonomyIds = JsonPath.from(response.asString()).getJsonObject("response[0].result.taxonomyId");
            assertFalse(taxonomyIds.isEmpty());

            assertTrue(missingField, m.containsKey("speciesCommonName"));
            assertTrue(missingField, m.containsKey("speciesScientificName"));
            assertTrue(missingField, m.containsKey("sourceType"));
            assertTrue(missingField, m.containsKey("center"));
            assertTrue(missingField, m.containsKey("material"));
            assertTrue(missingField, m.containsKey("scope"));
            assertTrue(missingField, m.containsKey("experimentType"));
            assertTrue(missingField, m.containsKey("experimentTypeAbbreviation"));
            assertTrue(missingField, m.containsKey("assembly"));
            assertTrue(missingField, m.containsKey("platform"));
            assertTrue(missingField, m.containsKey("url"));
            assertTrue(missingField, m.containsKey("publications"));
            assertTrue(missingField, m.containsKey("numVariants"));
            assertTrue(missingField, m.containsKey("numSamples"));
        }
    }

    @Test
    public void testGetStudiesStructural() throws URISyntaxException {
        ResponseEntity<QueryResponse> response = this.restTemplate.getForEntity("/v1/meta/studies/all?structural=true", QueryResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List queryResponse = response.getBody().getResponse();
        assertEquals(1, queryResponse.size());

        List<Map> result = (List<Map>)((Map)queryResponse.get(0)).get("result");
        assertTrue(result.size() >= 1);

        for (Map m : result) {
            String missingField = String.format("%s required field missing", m.get("name"));

            assertTrue(missingField, m.containsKey("name"));
            assertTrue(missingField, m.containsKey("id"));
            assertTrue(missingField, m.containsKey("description"));

            assertTrue(missingField, m.containsKey("taxonomyId"));
            List<Integer> taxonomyIds = (List<Integer>)m.get("taxonomyId");
//            List<Integer> taxonomyIds = JsonPath.from(response.asString()).getJsonObject("response[0].result.taxonomyId");
            assertFalse(taxonomyIds.isEmpty());

            assertTrue(missingField, m.containsKey("speciesCommonName"));
            assertTrue(missingField, m.containsKey("speciesScientificName"));
            assertTrue(missingField, m.containsKey("type"));
            assertTrue(missingField, m.containsKey("typeName"));
            assertTrue(missingField, m.containsKey("experimentType"));
            assertTrue(missingField, m.containsKey("assembly"));
            assertTrue(missingField, m.containsKey("publications"));
            assertTrue(missingField, m.containsKey("numVariants"));
            assertTrue(missingField, m.containsKey("numSamples"));
        }
    }

    @Test
    public void testGetStudiesStats() throws URISyntaxException {
        ResponseEntity<QueryResponse> response = this.restTemplate.getForEntity("/v1/meta/studies/stats", QueryResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List queryResponse = response.getBody().getResponse();
        assertEquals(1, queryResponse.size());

        List<Map> result = (List<Map>)((Map)queryResponse.get(0)).get("result");
        Map<String, Integer> species = (Map<String, Integer>)result.get(0).get("species");
//        Map<String, Integer> species = JsonPath.from(response.asString()).getJsonObject("response[0].result[0].species");
        assertTrue(species.size() >= 1);

        // instanceof are necessary to make it really evaluate the types
        for (Map.Entry<String, Integer> m : species.entrySet()) {
            assertTrue(m.getKey() instanceof String);
            assertTrue(m.getValue() instanceof Integer);
        }

        Map<String, Integer> types = (Map<String, Integer>)result.get(0).get("type");
//        Map<String, Integer> types = JsonPath.from(response.asString()).getJsonObject("response[0].result[0].type");
        assertTrue(types.size() >= 1);

        // instanceof is necessary to make it really evaluate the types
        for (Map.Entry<String, Integer> m : types.entrySet()) {
            assertTrue(m.getKey() instanceof String);
            assertTrue(m.getValue() instanceof Integer);
        }
    }

    @Test
    public void testGetStudiesStatsStructural() throws URISyntaxException {
        ResponseEntity<QueryResponse> response = this.restTemplate.getForEntity("/v1/meta/studies/stats?structural=true", QueryResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List queryResponse = response.getBody().getResponse();
        assertEquals(1, queryResponse.size());

        List<Map> result = (List<Map>)((Map)queryResponse.get(0)).get("result");
        Map<String, Integer> species = (Map<String, Integer>)result.get(0).get("species");
//        Map<String, Integer> species = JsonPath.from(response.asString()).getJsonObject("response[0].result[0].species");
        assertTrue(species.size() >= 1);

        // instanceof are necessary to make it really evaluate the types
        for (Map.Entry<String, Integer> m : species.entrySet()) {
            assertTrue(m.getKey() instanceof String);
            assertTrue(m.getValue() instanceof Integer);
        }

        Map<String, Integer> types = (Map<String, Integer>)result.get(0).get("type");
//        Map<String, Integer> types = JsonPath.from(response.asString()).getJsonObject("response[0].result[0].type");
        assertTrue(types.size() >= 1);

        // instanceof is necessary to make it really evaluate the types
        for (Map.Entry<String, Integer> m : types.entrySet()) {
            assertTrue(m.getKey() instanceof String);
            assertTrue(m.getValue() instanceof Integer);
        }
    }

}
