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
package uk.ac.ebi.dgva.server.ws;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties")
@Sql({ "classpath:init-data.sql" })
public class WSServerIntegrationTest {

    private static final String EXISTING_STUDY = "nstd49";

    private static final String NOT_EXISTING_STUDY = "nstd499";

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String allStudiesUrl = "/v1/meta/studies/all";

    @Test
    public void testGetStudies() {
        QueryResponse<QueryResult<VariantStudy>> queryResponse = executeGetCall(allStudiesUrl);
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantStudy> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(205, results.size());
    }

    @Test
    public void testGetExistingStudySummary() {
        QueryResponse<QueryResult<VariantStudy>> queryResponse = executeGetCall(
                "/v1/studies/" + EXISTING_STUDY + "/summary");
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantStudy> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(1, results.size());
    }


    @Test
    public void testGetNotExistingStudySummary() {
        QueryResponse<QueryResult<VariantStudy>> queryResponse = executeGetCall(
                "/v1/studies/" + NOT_EXISTING_STUDY + "/summary");
        assertEquals(1, queryResponse.getResponse().size());

        List<VariantStudy> results = queryResponse.getResponse().get(0).getResult();
        assertEquals(0, results.size());
    }

    private QueryResponse<QueryResult<VariantStudy>> executeGetCall(String allStudiesUrl) {
        ResponseEntity<QueryResponse<QueryResult<VariantStudy>>> response = restTemplate.exchange(
                allStudiesUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantStudy>>>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());

        return response.getBody();
    }

}
