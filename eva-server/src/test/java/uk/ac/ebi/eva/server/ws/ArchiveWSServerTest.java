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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.eva.commons.core.models.StudyType;
import uk.ac.ebi.eva.commons.mongodb.projections.VariantStudySummary;
import uk.ac.ebi.eva.commons.mongodb.services.VariantStudySummaryService;
import uk.ac.ebi.eva.lib.metadata.ArchiveDgvaDBAdaptor;
import uk.ac.ebi.eva.lib.metadata.ArchiveEvaproDBAdaptor;
import uk.ac.ebi.eva.lib.metadata.StudyDgvaDBAdaptor;
import uk.ac.ebi.eva.lib.metadata.StudyEvaproDBAdaptor;
import uk.ac.ebi.eva.lib.models.Assembly;
import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArchiveWSServerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ArchiveEvaproDBAdaptor archiveEvaproDBAdaptor;

    @MockBean
    private ArchiveDgvaDBAdaptor archiveDgvaDBAdaptor;

    @MockBean
    private StudyEvaproDBAdaptor studyEvaproDBAdaptor;

    @MockBean
    private StudyDgvaDBAdaptor studyDgvaDBAdaptor;

    // TODO: merge the three study adaptors into one?

    @MockBean
    private VariantStudySummaryService service;

    @Before
    public void setup() throws URISyntaxException, IOException, IllegalOpenCGACredentialsException {
        // species test data
        Assembly grch37 = new Assembly("GCA_000001405.1", "GCA_000001405", "1", "GRCh37", "grc3h7", 9606, "Human", "Homo Sapiens", "hsapiens", "human");

        Assembly grch38 = new Assembly("GCA_000001405.18", "GCA_000001405", "18", "GRCh38.p3", "grc3h8", 9606, "Human",
                              "Homo Sapiens", "hsapiens", "human");
        Assembly umd31 = new Assembly("GCA_000003055.3", "GCA_000003055", "3", "Bos_taurus_UMD_3.1", "umd31", 9913, "Cattle",
                             "Bos taurus", "btaurus", "cow");
        given(this.archiveEvaproDBAdaptor.getSpecies())
                .willReturn(encapsulateInQueryResult(grch37, grch38, umd31));
        given(this.archiveEvaproDBAdaptor.countSpecies()).willReturn(encapsulateInQueryResult(3L));

        given(this.archiveEvaproDBAdaptor.countFiles()).willReturn(encapsulateInQueryResult(5L));

        given(this.archiveEvaproDBAdaptor.countStudies()).willReturn(encapsulateInQueryResult(3L));


        VariantStudy study1 = new VariantStudy("Human Test study 1", "S1", null, "Study 1 description", new int[]{9606},
                                               "Human", "Homo Sapiens", "Germline", "EBI", "DNA", "multi-isolate",
                                               StudyType.CASE_CONTROL, "Exome Sequencing", "ES", "GRCh37",
                                               "GCA_000001405.3", "Illumina", new URI("http://www.s1.org"),
                                               new String[]{"10"}, 1000, 10);
        VariantStudy study2 = new VariantStudy("Human Test study 2", "S2", null, "Study 2 description", new int[]{9606},
                                               "Human", "Homo Sapiens", "Germline", "EBI", "DNA", "multi-isolate",
                                               StudyType.AGGREGATE, "Exome Sequencing", "ES", "GRCh38",
                                               "GCA_000001405.14", "Illumina", new URI("http://www.s2.org"),
                                               new String[]{"13"}, 5000, 4);
        VariantStudy study3 = new VariantStudy("Cow Test study 1", "CS1", null, "Cow study 1 description",
                                               new int[]{9913}, "Cow", "Bos taurus", "Germline", "EBI", "DNA",
                                               "multi-isolate", StudyType.AGGREGATE, "Whole Genome Sequencing",
                                               "WGSS", "Bos_taurus_UMD_3.1", "GCA_000003055.3", "Illumina",
                                               new URI("http://www.cs1.org"), new String[]{"1", "2"}, 1300, 12);
        given(studyEvaproDBAdaptor.getAllStudies(anyObject()))
                .willReturn(encapsulateInQueryResult(study1, study2, study3));
        Map<String, Long> studiesGroupedBySpeciesName = Stream.of(study1, study2, study3).collect(
                Collectors.groupingBy(VariantStudy::getSpeciesCommonName,
                                      Collectors.counting()));
        given(archiveEvaproDBAdaptor.countStudiesPerSpecies(anyObject()))
                .willReturn(encapsulateInQueryResult(studiesGroupedBySpeciesName.entrySet().toArray()));
        Map<String, Long> studiesGroupedByStudyType = Stream.of(study1, study2, study3).map(s -> s.getType().toString())
                                                       .collect(Collectors.groupingBy(Function.identity(),
                                                                                      Collectors.counting()));
        given(archiveEvaproDBAdaptor.countStudiesPerType(anyObject()))
                .willReturn(encapsulateInQueryResult(studiesGroupedByStudyType.entrySet().toArray()));


        VariantStudy svStudy1 = new VariantStudy("Human SV Test study 1", "svS1", null, "SV study 1 description",
                                                 new int[]{9606}, "Human", "Homo Sapiens", "Germline", "EBI", "DNA",
                                                 "multi-isolate", StudyType.CASE_CONTROL, "Exome Sequencing", "ES",
                                                 "GRCh37", "GCA_000001405.3", "Illumina", new URI("http://www.s1.org"),
                                                 new String[]{"10"}, 1000, 10);
        VariantStudy svStudy2 = new VariantStudy("Human SVV Test study 2", "svS2", null, "SV study 2 description",
                                                 new int[]{9606}, "Human", "Homo Sapiens", "Germline", "EBI", "DNA",
                                                 "multi-isolate", StudyType.AGGREGATE, "Exome Sequencing", "ES",
                                                 "GRCh38", "GCA_000001405.14", "Illumina", new URI("http://www.s2.org"),
                                                 new String[]{"13"}, 5000, 4);
        VariantStudy svStudy3 = new VariantStudy("Cow SV Test study 1", "svCS1", null, "SV cow study 1 description",
                                                 new int[]{9913}, "Cow", "Bos taurus", "Germline", "EBI", "DNA",
                                                 "multi-isolate", StudyType.AGGREGATE, "Whole Genome Sequencing", "WGS",
                                                 "Bos_taurus_UMD_3.1", "GCA_000003055.3", "Illumina",
                                                 new URI("http://www.cs1.org"), new String[]{"1", "2"}, 1300, 12);
        given(studyDgvaDBAdaptor.getAllStudies(anyObject()))
                  .willReturn(encapsulateInQueryResult(svStudy1, svStudy2, svStudy3));

        Map<String, Long> svStudiesGroupedBySpeciesName = Stream.of(svStudy1, svStudy2, svStudy3)
                .collect(Collectors.groupingBy(VariantStudy::getSpeciesCommonName, Collectors.counting()));
        given(archiveDgvaDBAdaptor.countStudiesPerSpecies(anyObject()))
                .willReturn(encapsulateInQueryResult(svStudiesGroupedBySpeciesName.entrySet().toArray()));

        Map<String, Long> svStudiesGroupedByStudyType = Stream.of(svStudy1, svStudy2, svStudy3)
                                                              .map(s -> s.getType().toString())
                                                              .collect(Collectors.groupingBy(Function.identity(),
                                                                                             Collectors.counting()));
        given(archiveDgvaDBAdaptor.countStudiesPerType(anyObject()))
                .willReturn(encapsulateInQueryResult(svStudiesGroupedByStudyType.entrySet().toArray()));

        List<VariantStudySummary> studies = buildVariantStudySummaries();
        given(service.findAll()).willReturn(studies);
    }

    private List<VariantStudySummary> buildVariantStudySummaries() {
        List<VariantStudySummary> studies = new ArrayList<>();
        VariantStudySummary study = new VariantStudySummary();
        study.setFilesCount(1);
        study.setStudyId("studyId");
        study.setStudyName("studyName");
        studies.add(study);
        return studies;
    }

    private <T> QueryResult<T> encapsulateInQueryResult(T... results) {
        return new QueryResult<>(null, 0, results.length, results.length, null, null, Arrays.asList(results));
    }


    @Test
    public void testCountSpecies() throws URISyntaxException {
        String url = "/v1/meta/species/count";
        ResponseEntity<QueryResponse<QueryResult<Integer>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<Integer>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<Integer>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<Integer> results = queryResponse.getResponse().get(0).getResult();
        assertTrue(results.size() >= 1);
        assertEquals(3, results.get(0).intValue());
    }

    @Test
    public void testGetSpecies() throws URISyntaxException {
        String url = "/v1/meta/species/list";
        ResponseEntity<QueryResponse<QueryResult<Assembly>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<Assembly>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<Assembly>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<Assembly> results = queryResponse.getResponse().get(0).getResult();
        assertTrue(results.size() >= 1);

        for (Assembly assembly : results) {
            assertFalse(assembly.getAssemblyCode().isEmpty());
            assertFalse(assembly.getAssemblyName().isEmpty());
            if (assembly.getAssemblyChain() != null) { // Accessioned assembly
                assertFalse(assembly.getAssemblyAccession().isEmpty());
                assertFalse(assembly.getAssemblyChain().isEmpty());
                assertFalse(assembly.getAssemblyVersion().isEmpty());
            }

            assertNotEquals(0, assembly.getTaxonomyId());
            assertFalse(assembly.getTaxonomyCode().isEmpty());
            assertFalse(assembly.getTaxonomyEvaName().isEmpty());
            assertFalse(assembly.getTaxonomyScientificName().isEmpty());
        }
    }

    @Test
    public void testCountFiles() throws URISyntaxException {
        String url = "/v1/meta/files/count";
        assertGetCount(url, 5);
    }

    @Test
    public void testCountStudies() throws URISyntaxException {
        String url = "/v1/meta/studies/count";
        assertGetCount(url, 3);
    }

    private void assertGetCount(String url, Integer expectedCount) {
        ResponseEntity<QueryResponse<QueryResult<Integer>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<Integer>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<Integer>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<Integer> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(1, results.size());

        assertEquals(expectedCount, results.get(0));
    }

    @Test
    public void testGetBrowsableStudiesNoSpecies() throws URISyntaxException {
        String url = "/v1/meta/studies/list";
        ResponseEntity<QueryResponse<QueryResult<VariantStudySummary>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantStudySummary>>>() {});
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        QueryResponse<QueryResult<VariantStudySummary>> queryResponse = response.getBody();
        assertEquals(0, queryResponse.getResponse().size());
    }

    @Test
    public void testGetBrowsableStudiesBySpecies() throws URISyntaxException {
        String url = "/v1/meta/studies/list?species=hsapiens_grch37";
        ResponseEntity<QueryResponse<QueryResult<VariantStudySummary>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantStudySummary>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<VariantStudySummary>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantStudySummary> results = queryResponse.getResponse().get(0).getResult();
        assertTrue(results.size() >= 1);

        for (VariantStudySummary variantStudySummary : results) {
            assertTrue(variantStudySummary.getFilesCount() > 0);
            assertNotNull(variantStudySummary.getStudyName());
            assertNotNull(variantStudySummary.getStudyId());
        }
    }

    @Test
    public void testGetStudies() throws URISyntaxException {
        String url = "/v1/meta/studies/all";
        assertGetStudiesAll(url);
    }

    @Test
    public void testGetStudiesStructural() throws URISyntaxException {
        String url = "/v1/meta/studies/all?structural=true";
        assertGetStudiesAll(url);
    }

    private void assertGetStudiesAll(String url) {
        ResponseEntity<QueryResponse<QueryResult<VariantStudy>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantStudy>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<VariantStudy>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantStudy> results = queryResponse.getResponse().get(0).getResult();
        assertTrue(results.size() >= 1);

        assertVariantStudiesAreNotEmpty(results);
    }

    private void assertVariantStudiesAreNotEmpty(List<VariantStudy> results) {
        for (VariantStudy variantStudy : results) {
            assertFalse(variantStudy.getName().isEmpty());
            assertFalse(variantStudy.getId().isEmpty());
            assertFalse(variantStudy.getDescription().isEmpty());

            assertNotNull(variantStudy.getTaxonomyId());
            assertNotEquals(0, variantStudy.getTaxonomyId().length);

            assertFalse(variantStudy.getSpeciesCommonName().isEmpty());
            assertFalse(variantStudy.getSpeciesScientificName().isEmpty());
            assertFalse(variantStudy.getSourceType().isEmpty());
            assertFalse(variantStudy.getCenter().isEmpty());
            assertFalse(variantStudy.getMaterial().isEmpty());
            assertFalse(variantStudy.getScope().isEmpty());
            assertFalse(variantStudy.getTypeName().isEmpty());
            assertFalse(variantStudy.getExperimentType().isEmpty());
            assertFalse(variantStudy.getExperimentTypeAbbreviation().isEmpty());
            assertFalse(variantStudy.getAssembly().isEmpty());
            assertFalse(variantStudy.getPlatform().isEmpty());
            assertNotNull(variantStudy.getUrl());
            assertNotNull(variantStudy.getPublications());
            assertNotEquals(0, variantStudy.getNumVariants());
            assertNotEquals(0, variantStudy.getNumSamples());
        }
    }

    @Test
    public void testGetStudiesStats() throws URISyntaxException {
        String url = "/v1/meta/studies/stats";
        assertGetStudiesStats(url);
    }

    @Test
    public void testGetStudiesStatsStructural() throws URISyntaxException {
        String url = "/v1/meta/studies/stats?structural=true";
        assertGetStudiesStats(url);
    }

    private void assertGetStudiesStats(String url) {
        ResponseEntity<QueryResponse<QueryResult<ObjectNode>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<ObjectNode>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<ObjectNode>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<ObjectNode> results = queryResponse.getResponse().get(0).getResult();
        assertTrue(results.size() >= 1);

        JsonNode species = results.get(0).get("species");
        assertTrue(species.size() >= 1);

        for (JsonNode eachSpecies : species) {
            assertTrue(eachSpecies.isIntegralNumber());
        }

        JsonNode types = results.get(0).get("type");
        assertTrue(types.size() >= 1);

        for (JsonNode type : types) {
            assertTrue(type.isIntegralNumber());
        }
    }

}
