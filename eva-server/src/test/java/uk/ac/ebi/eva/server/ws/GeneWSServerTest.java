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
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variant.annotation.Xref;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;
import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GeneWSServerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantEntityRepository variantEntityRepository;

    @MockBean
    private DBAdaptorConnector dbAdaptorConnector;

    private String GENE_ID = "GeneId";
    private VariantEntity testVariantEntity;

    @Before
    public void setUp() throws Exception {
        String chromosome = "1";
        int start = 1000;
        int end = 1005;
        String reference = "reference";
        String alternate = "alternate";
        testVariantEntity = new VariantEntity(chromosome, start, end, reference, alternate);
        VariantAnnotation variantAnnotation = new VariantAnnotation(chromosome, start, end, reference, alternate);
        variantAnnotation.setXrefs(Collections.singletonList(new Xref(GENE_ID, "HGNC")));
        testVariantEntity.setAnnotation(variantAnnotation);
        List<VariantEntity> variantEntities = Collections.singletonList(testVariantEntity);

        List<String> geneIds = new ArrayList<>();
        geneIds.add(GENE_ID);

        given(variantEntityRepository.findByGenesAndComplexFilters(eq(geneIds), any(), null, any()
        )).willReturn(variantEntities);
        given(variantEntityRepository.countByGenesAndComplexFilters(eq(geneIds), any())).willReturn(1L);
    }

    @Test
    public void testGetVariantByGeneExisting() {
        QueryResponse<QueryResult<VariantEntity>> queryResponse =
                testGetVariantByGeneHelper(Collections.singletonList(GENE_ID));
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantEntity> results = queryResponse.getResponse().get(0).getResult();

        assertEquals(1, results.size());
        assertEquals(testVariantEntity, results.get(0));
    }

    @Test
    public void testGetVariantByGeneNotExisting() {
        QueryResponse<QueryResult<VariantEntity>> queryResponse =
                testGetVariantByGeneHelper(Collections.singletonList("not_a_real_id"));
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantEntity> results = queryResponse.getResponse().get(0).getResult();

        assertEquals(0, results.size());
    }

    private QueryResponse<QueryResult<VariantEntity>> testGetVariantByGeneHelper(List<String> geneIds) {
        String url = "/v1/genes/" + String.join(",", geneIds) + "/variants?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<VariantEntity>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantEntity>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());

        return response.getBody();
    }
}
