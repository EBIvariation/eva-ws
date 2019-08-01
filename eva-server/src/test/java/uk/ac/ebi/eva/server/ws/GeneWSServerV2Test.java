/*
 * Copyright 2019 EMBL - European Bioinformatics Institute
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.eva.commons.core.models.FeatureCoordinates;
import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.FeatureService;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GeneWSServerV2Test {

    private static final String MAIN_ID = "rs1";

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FeatureService featureService;

    @MockBean
    private VariantWithSamplesAndAnnotationsService variantService;

    @Autowired
    private ObjectMapper objectMapper;

    private String GENE_ID1 = "ENSG00000227232";

    private String GENE_ID2 = "ENSG00000227244";

    @Before
    public void setUp() throws Exception {
        VariantWithSamplesAndAnnotation variantEntity = new VariantWithSamplesAndAnnotation("20", 1000, 1005,
                "reference", "alternate", MAIN_ID);

        List<Region> oneRegion = Collections.singletonList(new Region("20", 60000L, 62000L));
        given(variantService.findByRegionsAndComplexFilters(eq(oneRegion), any(), any(), any(), any()))
                .willReturn(Collections.singletonList(variantEntity));
        given(variantService.countByRegionsAndComplexFilters(eq(oneRegion), any())).willReturn(1L);

        List<Region> twoRegions = Arrays.asList(new Region("20", 60000L, 62000L),
                new Region("20", 63000L, 64000L));


        given(variantService.findByRegionsAndComplexFilters(eq(twoRegions), any(), any(), any(), any()))
                .willReturn(Arrays.asList(variantEntity, variantEntity));
        given(variantService.countByRegionsAndComplexFilters(eq(twoRegions), any())).willReturn(2l);

        given(variantService
                .findByRegionsAndComplexFilters(not(or(eq(oneRegion), eq(twoRegions))), any(), any(), any(), any()))
                .willReturn(Collections.emptyList());
        given(variantService.countByRegionsAndComplexFilters(not(or(eq(oneRegion), eq(twoRegions))), any()))
                .willReturn(0l);

        FeatureCoordinates feature1 = new FeatureCoordinates(GENE_ID1, "id", "feature", "20", 60000L, 62000L);
        FeatureCoordinates feature2 = new FeatureCoordinates(GENE_ID1, "id", "feature", "20", 63000L, 64000L);

        List<FeatureCoordinates> featureCoordinate = Arrays.asList(feature1);
        List<String> geneId = Arrays.asList(GENE_ID1);
        given(featureService.findAllByGeneIdsOrGeneNames(eq(geneId), eq(geneId))).willReturn(featureCoordinate);

        List<FeatureCoordinates> featureCoordinates = Arrays.asList(feature1, feature2);
        List<String> geneIds = Arrays.asList(GENE_ID1, GENE_ID2);
        given(featureService.findAllByGeneIdsOrGeneNames(eq(geneIds), eq(geneIds))).willReturn(featureCoordinates);
    }

    @Test
    public void testGetVariantsByExistingGene() throws URISyntaxException {
        assertEquals("20", testGetVariantsGeneHelper("ENSG00000227232", 1, HttpStatus.OK).get(0));
    }

    private List<String> testGetVariantsByGeneHelper(String testRegion, int expectedVariants, HttpStatus status)
            throws URISyntaxException {
        String url = "/v2/genes/" + testRegion + "/variants?species=mmusculus&assembly=grcm38";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(status, response.getStatusCode());
        if (status == HttpStatus.NO_CONTENT) {
            assertNull(response.getBody());
            return null;
        }
        Configuration configuration = Configuration.defaultConfiguration()
                .jsonProvider(new JacksonJsonProvider())
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .addOptions(Option.SUPPRESS_EXCEPTIONS);
        List<Variant> variantList = JsonPath.using(configuration).parse(response.getBody())
                .read("$['_embedded']['variantList']", new TypeRef<List<Variant>>() {
                });

        if (variantList == null) {
            assertEquals(expectedVariants, 0);
            return null;
        }
        assertEquals(expectedVariants, variantList.size());
        List<String> chromosomes = new ArrayList<>();
        variantList.forEach(variant -> {
            chromosomes.add(variant.getChromosome());
        });
        return chromosomes;
    }

    @Test
    public void testGetVariantsByExistingGenes() throws URISyntaxException {
        List<String> chromosomes = testGetVariantsGeneHelper("ENSG00000227232,ENSG00000227244", 2, HttpStatus.OK);
        assertEquals("20", chromosomes.get(0));
        assertEquals("20", chromosomes.get(1));
    }

    @Test
    public void testGetVariantsByNonExistingGene() throws URISyntaxException {
        testGetVariantsGeneHelper("nonexisting", 0, HttpStatus.NO_CONTENT);
    }

    @Test
    public void testGetVariantsByNonExistingGenes() throws URISyntaxException {
        testGetVariantsGeneHelper("nonexisting,nonexisting", 0, HttpStatus.NO_CONTENT);
    }
}
