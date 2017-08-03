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
package uk.ac.ebi.dgva.server.ws;

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
import uk.ac.ebi.eva.lib.metadata.dgva.ArchiveDgvaDBAdaptor;
import uk.ac.ebi.eva.lib.metadata.dgva.StudyDgvaDBAdaptor;
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
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArchiveWSServerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ArchiveDgvaDBAdaptor archiveDgvaDBAdaptor;

    @MockBean
    private StudyDgvaDBAdaptor studyDgvaDBAdaptor;

    @Before
    public void setup() throws URISyntaxException, IOException {
        VariantStudy svStudy1 = new VariantStudy("Human SV Test study 1", "svS1", null, "SV study 1 description",
                                                 new int[]{9606},
                                                 "Human", "Homo Sapiens", "Germline", "EBI", "DNA", "multi-isolate",
                                                 StudyType.CASE_CONTROL, "Exome Sequencing", "ES",
                                                 "GRCh37", "GCA_000001405.3", "Illumina", new URI("http://www.s1.org"), new String[]{"10"},
                                                 1000, 10);
        VariantStudy svStudy2 = new VariantStudy("Human SVV Test study 2", "svS2", null, "SV study 2 description",
                                                 new int[]{9606}, "Human", "Homo Sapiens", "Germline", "EBI", "DNA",
                                                 "multi-isolate", StudyType.AGGREGATE, "Exome Sequencing",
                                                 "ES", "GRCh38", "GCA_000001405.14", "Illumina", new URI("http://www.s2.org"),
                                                 new String[]{"13"}, 5000, 4);
        VariantStudy svStudy3 = new VariantStudy("Cow SV Test study 1", "svCS1", null, "SV cow study 1 description",
                                                 new int[]{9913}, "Cow", "Bos taurus", "Germline", "EBI", "DNA",
                                                 "multi-isolate", StudyType.AGGREGATE,
                                                 "Whole Genome Sequencing", "WGSS", "Bos_taurus_UMD_3.1", "GCA_000003055.3", "Illumina",
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
    }

    private <T> QueryResult<T> encapsulateInQueryResult(T... results) {
        return new QueryResult<>(null, 0, results.length, results.length, null, null, Arrays.asList(results));
    }

    @Test
    public void testGetStudies() throws URISyntaxException {
        String url = "/v1/meta/studies/all";
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
        assertEquals(3, results.size());

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
            assertFalse(variantStudy.getExperimentType().isEmpty());
            assertFalse(variantStudy.getExperimentTypeAbbreviation().isEmpty());
            assertFalse(variantStudy.getAssembly().isEmpty());
            assertFalse(variantStudy.getPlatform().isEmpty());
            assertNotNull(variantStudy.getUrl());
            assertNotNull(variantStudy.getPublications());
            assertNotEquals(0, variantStudy.getNumSamples());
        }
    }
}
