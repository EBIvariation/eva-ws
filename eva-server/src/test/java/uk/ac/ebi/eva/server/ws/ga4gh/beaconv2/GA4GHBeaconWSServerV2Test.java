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

package uk.ac.ebi.eva.server.ws.ga4gh.beaconv2;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.eva.commons.beacon.models.BeaconAlleleRequest;
import uk.ac.ebi.eva.commons.beacon.models.BeaconAlleleResponse;
import uk.ac.ebi.eva.commons.beacon.models.Chromosome;
import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.VariantType;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantMongo;
import uk.ac.ebi.eva.commons.mongodb.filter.FilterBuilder;
import uk.ac.ebi.eva.commons.mongodb.filter.VariantRepositoryFilter;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GA4GHBeaconWSServerV2Test {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantWithSamplesAndAnnotationsService service;

    @Before
    public void setup() throws Exception {
        VariantMongo variantMongo = new VariantMongo(null, "X", 100470026, 100470026, 1, "G", "A");
        List<VariantMongo> variantMongoList = Collections.singletonList(variantMongo);
        Region startRange = new Region("X", 100470026L, 100470026L);
        Region endRange = new Region("X", 100470026L, 100470026L);
        List<VariantRepositoryFilter> variantRepositoryFilters = new FilterBuilder().getBeaconFilters("G", "A",
               null, Arrays.asList("PRJEB7218"));

        Pageable pageable = new PageRequest(0, 1);
        given(service.findByRegionAndOtherBeaconFilters(eq(startRange), eq(endRange), eq(variantRepositoryFilters),
                eq(pageable))).willReturn(variantMongoList);
        given(service.countByRegionAndOtherBeaconFilters(eq(startRange), eq(endRange), eq(variantRepositoryFilters)))
                .willReturn(1L);

        variantRepositoryFilters = new FilterBuilder().getBeaconFilters("G", null,
                VariantType.SNV, Arrays.asList("PRJEB7218"));
        given(service.findByRegionAndOtherBeaconFilters(eq(startRange), eq(endRange), eq(variantRepositoryFilters),
                eq(pageable))).willReturn(variantMongoList);
        given(service.countByRegionAndOtherBeaconFilters(eq(startRange), eq(endRange), eq(variantRepositoryFilters)))
                .willReturn(1L);
    }

    @Test
    public void testForExisting() throws Exception {
        BeaconAlleleRequest request = new BeaconAlleleRequest();
        request.setReferenceName(Chromosome.X);
        request.setAssemblyId("GRCh37");
        request.setReferenceBases("G");
        request.setStart(100470026L);
        request.setEnd(100470026);
        request.setAlternateBases("A");
        request.setVariantType("SNV");
        request.setDatasetIds(Arrays.asList("PRJEB7218"));

        String url = UriComponentsBuilder.fromUriString("")
                .path("/v2/beacon/query")
                .queryParam("referenceName", request.getReferenceName())
                .queryParam("referenceBases", request.getReferenceBases())
                .queryParam("assemblyId", request.getAssemblyId())
                .queryParam("alternateBases", request.getAlternateBases())
                .queryParam("start", request.getStart())
                .queryParam("end", request.getEnd())
                .queryParam("datasetIds", String.join(",", request.getDatasetIds()))
                .build().toString();

        assertTrue(testBeaconHelper(url).getBody().get(0).isExists());
        request.setStartMin(1);
        request.setStartMax(1);
        request.setEndMin(1);
        request.setEndMax(1);

        url = UriComponentsBuilder.fromUriString("")
                .path("/v2/beacon/query")
                .queryParam("referenceName", request.getReferenceName())
                .queryParam("referenceBases", request.getReferenceBases())
                .queryParam("assemblyId", request.getAssemblyId())
                .queryParam("start", request.getStart())
                .queryParam("end", request.getEnd())
                .queryParam("startMin", request.getStartMin())
                .queryParam("startMax", request.getStartMax())
                .queryParam("endMin", request.getEndMin())
                .queryParam("endMax", request.getEndMax())
                .queryParam("variantType", request.getVariantType())
                .queryParam("datasetIds", String.join(",", request.getDatasetIds()))
                .build().toString();
        assertTrue(testBeaconHelper(url).getBody().get(0).isExists());
    }

    @Test
    public void testForNonExisting() {
        BeaconAlleleRequest request = new BeaconAlleleRequest();
        request.setReferenceName(Chromosome.Y);
        request.setAssemblyId("GRCh37");
        request.setReferenceBases("G");
        request.setAlternateBases("A");

        String url = UriComponentsBuilder.fromUriString("")
                .path("/v2/beacon/query")
                .queryParam("referenceName", request.getReferenceName())
                .queryParam("referenceBases", request.getReferenceBases())
                .queryParam("assemblyId", request.getAssemblyId())
                .queryParam("alternateBases", request.getAlternateBases())
                .build().toString();
        assertFalse(testBeaconHelper(url).getBody().get(0).isExists());
    }

    @Test
    public void testForError() {
        String url = UriComponentsBuilder.fromUriString("")
                .path("/v2/beacon/query")
                .queryParam("referenceName", "X")
                .queryParam("referenceBases", "G")
                .queryParam("assemblyId", "GRch37")
                .build().toString();
        assertEquals(400, testBeaconHelper(url).getBody().get(0).getError().getErrorCode().intValue());
        assertEquals("Either the alternate bases or the variant type is required", testBeaconHelper(url).
                getBody().get(0).getError().getErrorMessage());
    }

    private ResponseEntity<List<BeaconAlleleResponse>> testBeaconHelper(String url) {
        ResponseEntity<List<BeaconAlleleResponse>> response = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<BeaconAlleleResponse>>() {
                });
        return response;
    }

    @Test
    public void testNonExisingForBothAlternateAndVariantType() {
        String url = UriComponentsBuilder.fromUriString("")
                .path("/v2/beacon/query")
                .queryParam("referenceName", Chromosome.X)
                .queryParam("referenceBases", "G")
                .queryParam("assemblyId", "GRCh37")
                .queryParam("alternateBases", "A")
                .queryParam("start", 100470026L)
                .queryParam("end", 100470026L)
                .queryParam("datasetIds" ,String.join(",", Arrays.asList("PRJEB7218")))
                .queryParam("variantType", "SNV")
                .build().toString();
        assertFalse(testBeaconHelper(url).getBody().get(0).isExists());
    }
}
