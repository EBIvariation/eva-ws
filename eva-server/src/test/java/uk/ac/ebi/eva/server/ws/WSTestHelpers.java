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

import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.ac.ebi.eva.commons.core.models.ws.VariantSourceEntryWithSampleNames;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

public class WSTestHelpers {

    public static JSONObject testRestTemplateHelperJsonObject(String url, TestRestTemplate restTemplate) {
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<String>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return new JSONObject(new JSONTokener(response.getBody()));
    }

    public static List<VariantWithSamplesAndAnnotation> testRestTemplateHelper(String url,
                                                                               TestRestTemplate restTemplate) {
        ResponseEntity<QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>>>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());

        QueryResponse<QueryResult<VariantWithSamplesAndAnnotation>> queryResponse = response.getBody();
        assertEquals(1, queryResponse.getResponse().size());

        return queryResponse.getResponse().get(0).getResult();
    }

    public static String testRestTemplateHelperForError(String url,
                                                        TestRestTemplate restTemplate) {
        ResponseEntity<QueryResponse<String> > response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<QueryResponse<String>>() {
                });
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
        return response.getBody().getError();
    }

    public static void checkVariantsInFullResults(List<VariantWithSamplesAndAnnotation> results,
                                                  int expectedVariants) {
        assertEquals(expectedVariants, results.size());

        for (VariantWithSamplesAndAnnotation variantEntity : results) {
            assertFalse(variantEntity.getChromosome().isEmpty());
            assertFalse(variantEntity.getReference().isEmpty());
            assertFalse(variantEntity.getAlternate().isEmpty());
            for (VariantSourceEntryWithSampleNames variantSourceEntry : variantEntity.getSourceEntries()) {
                assertFalse(variantSourceEntry.getCohortStats().isEmpty());
            }
            assertNotEquals(0, variantEntity.getStart());
            assertNotEquals(0, variantEntity.getEnd());
        }
    }
}
