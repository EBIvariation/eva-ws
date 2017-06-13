/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2016 EMBL - European Bioinformatics Institute
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
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

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;

/**
 * Tests for VariantWSServer
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VariantWSServerTest {

    private static final String CHROMOSOME = "existingChromosome";

    private static final String VARIANT_ID = "existingId";

    private static final String NON_EXISTING_VARIANT_ID = "notARealId";

    private static final String NON_EXISTING_CHROMOSOME = "notARealChromosome";

    private static final VariantEntity VARIANT = new VariantEntity("1", 1000, 1005, "reference", "alternate");

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantEntityRepository variantEntityRepository;

    @MockBean
    private VariantDBAdaptor variantMongoDbAdaptor;

    @Before
    public void setUp() throws Exception {
        List<VariantEntity> variantEntities = Collections.singletonList(VARIANT);

        given(variantEntityRepository
                .findByChromosomeAndStartAndReferenceAndAlternate(eq(CHROMOSOME), anyInt(), any(), any()))
                .willReturn(variantEntities);

        given(variantEntityRepository.findByIdsAndComplexFilters(eq(VARIANT_ID), any(), any(), any()))
                .willReturn(variantEntities);

        Region region = new Region(CHROMOSOME, 1, 1);
        Region badRegion = new Region(NON_EXISTING_CHROMOSOME, 1, 1);

        QueryResult<Variant> queryResult = new QueryResult<>();
        queryResult.setNumResults(1);
        given(variantMongoDbAdaptor.getAllVariantsByRegion(eq(region), any())).willReturn(queryResult);
        given(variantMongoDbAdaptor.getAllVariantsByRegion(eq(badRegion), any())).willReturn(new QueryResult<>());
    }

    @Test
    public void testGetVariantById() {
        testGetVariantByIdRegionHelper(VARIANT_ID);
    }

    @Test
    public void testGetVariantByRegion() {
        testGetVariantByIdRegionHelper(CHROMOSOME + ":71822:C:G");
    }

    private void testGetVariantByIdRegionHelper(String testString) {
        String url = "/v1/variants/" + testString + "/info?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<VariantEntity>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantEntity>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<VariantEntity>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantEntity> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(1, results.size());

        assertEquals(VARIANT, results.get(0));
    }

    @Test
    public void testGetVariantByIdDoesntExist() throws URISyntaxException {
        testGetVariantByIdRegionDoesntExistHelper(NON_EXISTING_VARIANT_ID);
    }

    @Test
    public void testGetVariantByRegionDoesntExist() throws URISyntaxException {
        testGetVariantByIdRegionDoesntExistHelper(NON_EXISTING_CHROMOSOME + ":71821:C:G");
    }

    private void testGetVariantByIdRegionDoesntExistHelper(String testString) throws URISyntaxException {
        String url = "/v1/variants/" + testString + "/info?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<VariantEntity>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantEntity>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<VariantEntity>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantEntity> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(0, results.size());
    }

    @Test
    public void testCheckVariantExistsDoesExistRegion() throws URISyntaxException {
        assertTrue(testCheckVariantExistsHelper(CHROMOSOME + ":1:C:G"));
    }

    @Test
    public void testCheckVariantExistsDoesntExistRegion() throws URISyntaxException {
        assertFalse(testCheckVariantExistsHelper(NON_EXISTING_CHROMOSOME + ":1:C:G"));
    }

    @Test
    public void testCheckVariantExistsDoesExistId() throws URISyntaxException {
        assertTrue(testCheckVariantExistsHelper(VARIANT_ID));
    }

    @Test
    public void testCheckVariantExistsDoesntExistId() throws URISyntaxException {
        assertFalse(testCheckVariantExistsHelper(NON_EXISTING_VARIANT_ID));
    }

    private Boolean testCheckVariantExistsHelper(String testIdRegion) throws URISyntaxException {
        String url = "/v1/variants/" + testIdRegion + "/exists?species=mmusculus_grcm38";
        ResponseEntity<QueryResponse<QueryResult<Boolean>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<Boolean>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<Boolean>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        List<Boolean> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(1, results.size());
        return results.get(0);
    }

}
