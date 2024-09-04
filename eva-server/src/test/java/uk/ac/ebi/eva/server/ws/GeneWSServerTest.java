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
import uk.ac.ebi.eva.commons.core.models.Annotation;
import uk.ac.ebi.eva.commons.core.models.Xref;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigNamingConvention;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.server.ws.contigalias.ContigAliasService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GeneWSServerTest {

    private static final String MAIN_ID = "rs1";

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantWithSamplesAndAnnotationsService service;

    @MockBean
    private ContigAliasService contigAliasService;

    private String GENE_ID = "GeneId";
    private VariantWithSamplesAndAnnotation testVariantEntity;

    @Before
    public void setUp() throws Exception {
        String chromosome = "1";
        long start = 10000000000L;
        long end = 10000000005L;
        String reference = "A";
        String alternate = "T";
        String vepVersion = "88";
        String vepCacheVersion = "89";
        testVariantEntity = new VariantWithSamplesAndAnnotation(chromosome, start, end, reference, alternate, MAIN_ID);
        Annotation variantAnnotation = new Annotation(chromosome, start, end, vepVersion, vepCacheVersion, Collections
                .singleton(new Xref(GENE_ID, "HGNC")), null);
        testVariantEntity.setAnnotation(variantAnnotation);
        List<VariantWithSamplesAndAnnotation> variantEntities = Collections.singletonList(testVariantEntity);

        List<String> geneIds = new ArrayList<>();
        geneIds.add(GENE_ID);

        given(service.findByGenesAndComplexFilters(eq(geneIds), any(), any(), any(), any())).willReturn(variantEntities);
        given(service.countByGenesAndComplexFilters(eq(geneIds), any())).willReturn(1L);
        given(contigAliasService.getVariantsWithTranslatedContig(Collections.singletonList(testVariantEntity), null))
                .willReturn(Collections.singletonList(testVariantEntity));
    }

    @Test
    public void testGetVariantByGeneExisting() {
        QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>> queryResponse =
                testGetVariantByGeneHelper(Collections.singletonList(GENE_ID), null);
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantWithSamplesAndAnnotation> results = queryResponse.getResponse().get(0).getResult();

        assertEquals(1, results.size());
        assertEquals(testVariantEntity, results.get(0));
        assertEquals(MAIN_ID, results.get(0).getMainId());
    }

    @Test
    public void testGetVariantByGeneExistingWithTranslatedContig() {
        VariantWithSamplesAndAnnotation translatedContig = new VariantWithSamplesAndAnnotation("2",
                10000000000L, 10000000005L, "A", "T", MAIN_ID);
        given(contigAliasService.getVariantsWithTranslatedContig(Collections.singletonList(testVariantEntity), ContigNamingConvention.ENA_SEQUENCE_NAME))
                .willReturn(Collections.singletonList(translatedContig));

        QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>> queryResponse =
                testGetVariantByGeneHelper(Collections.singletonList(GENE_ID), ContigNamingConvention.ENA_SEQUENCE_NAME);
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantWithSamplesAndAnnotation> results = queryResponse.getResponse().get(0).getResult();

        assertEquals(1, results.size());
        assertEquals(translatedContig, results.get(0));
        assertEquals(MAIN_ID, results.get(0).getMainId());
        assertEquals("2", results.get(0).getChromosome());
    }

    @Test
    public void testGetVariantByGeneNotExisting() {
        QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>> queryResponse =
                testGetVariantByGeneHelper(Collections.singletonList("not_a_real_id"), null);
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantWithSamplesAndAnnotation> results = queryResponse.getResponse().get(0).getResult();

        assertEquals(0, results.size());
    }

    private QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>> testGetVariantByGeneHelper(List<String> geneIds,
                                                                                                   ContigNamingConvention contigNamingConvention) {
        String url = "/v1/genes/" + String.join(",", geneIds) + "/variants?species=mmusculus_grcm38";
        if (contigNamingConvention != null) {
            url += "&contigNamingConvention=" + contigNamingConvention;
        }
        String responseStr = restTemplate.getForObject(url, String.class);
        ResponseEntity<QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>>>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());

        return response.getBody();
    }
}
