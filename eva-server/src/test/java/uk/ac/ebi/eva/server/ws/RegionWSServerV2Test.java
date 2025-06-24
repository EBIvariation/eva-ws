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
import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigAliasChromosome;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigNamingConvention;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.utils.TaxonomyUtils;
import uk.ac.ebi.eva.server.ws.contigalias.ContigAliasService;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegionWSServerV2Test {

    private static final String MAIN_ID = "rs1";

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantWithSamplesAndAnnotationsService service;

    @MockBean
    private ContigAliasService contigAliasService;

    @MockBean
    private TaxonomyUtils taxonomyUtils;

    @Autowired
    private ObjectMapper objectMapper;

    VariantWithSamplesAndAnnotation variantEntity = new VariantWithSamplesAndAnnotation("chr1", 1000, 1005,
            "reference", "alternate", MAIN_ID);

    @Before
    public void setUp() throws Exception {
        List<Region> oneRegion = Collections.singletonList(new Region("20", 60000L, 62000L));
        given(service.findByRegionsAndComplexFilters(eq(oneRegion), any(), any(), any(), any()))
                .willReturn(Collections.singletonList(variantEntity));
        given(service.countByRegionsAndComplexFilters(eq(oneRegion), any())).willReturn(1l);

        List<Region> twoRegions = Arrays.asList(new Region("20", 60000L, 61000L),
                new Region("20", 61500L, 62500L));

        given(service.findByRegionsAndComplexFilters(eq(twoRegions), any(), any(), any(), any()))
                .willReturn(Arrays.asList(variantEntity, variantEntity));
        given(service.countByRegionsAndComplexFilters(eq(twoRegions), any())).willReturn(2l);

        given(service
                .findByRegionsAndComplexFilters(not(or(eq(oneRegion), eq(twoRegions))), any(), any(), any(), any()))
                .willReturn(Collections.emptyList());
        given(service.countByRegionsAndComplexFilters(not(or(eq(oneRegion), eq(twoRegions))), any())).willReturn(0l);

        ContigAliasChromosome contigAliasChromosome = new ContigAliasChromosome();
        contigAliasChromosome.setInsdcAccession("20");
        given(contigAliasService.getUniqueInsdcChromosomeByName("20", "GCA_000001635.2",
                null)).willReturn(contigAliasChromosome);
        given(contigAliasService.getUniqueInsdcChromosomeByName("20", "GCA_000001635.2",
                ContigNamingConvention.ENA_SEQUENCE_NAME)).willReturn(contigAliasChromosome);
        given(contigAliasService.getMatchingContigNamingConvention(contigAliasChromosome, "20"))
                .willReturn(ContigNamingConvention.INSDC);

        given(taxonomyUtils.getAssemblyAccessionForAssemblyCode("grcm38")).willReturn(Optional.of("GCA_000001635.2"));
    }

    @Test
    public void testGetVariantsByExistingRegion() throws URISyntaxException {
        testGetVariantsByRegionHelper("20:60000-62000", 1, HttpStatus.OK);
    }

    @Test
    public void testGetVariantsByExistingRegionWithTranslatedContig() throws Exception {
        VariantWithSamplesAndAnnotation variant = new VariantWithSamplesAndAnnotation("20", 61000, 61005,
                "reference", "alternate", MAIN_ID);
        List<Region> oneRegion = Collections.singletonList(new Region("1", 60000L, 62000L));
        given(service.countByRegionsAndComplexFilters(eq(oneRegion), any())).willReturn(1l);
        List<Region> translatedRegion = Collections.singletonList(new Region("20", 60000L, 62000L));
        given(service.findByRegionsAndComplexFilters(eq(translatedRegion), any(), any(), any(), any()))
                .willReturn(Collections.singletonList(variant));

        ContigAliasChromosome contigAliasChromosome = new ContigAliasChromosome();
        contigAliasChromosome.setInsdcAccession("20");
        contigAliasChromosome.setEnaSequenceName("1");
        given(contigAliasService.getUniqueInsdcChromosomeByName("1", "GCA_000001635.2",
                ContigNamingConvention.ENA_SEQUENCE_NAME)).willReturn(contigAliasChromosome);
        given(contigAliasService.getMatchingContigNamingConvention(contigAliasChromosome, "1"))
                .willReturn(ContigNamingConvention.ENA_SEQUENCE_NAME);

        List<Variant> results = regionWsHelper("1:60000-62000", HttpStatus.OK, ContigNamingConvention.ENA_SEQUENCE_NAME);

        assertEquals(1, results.size());
        results.forEach(variantEntity -> {
            assertEquals("1", variantEntity.getChromosome());
            assertFalse(variantEntity.getReference().isEmpty());
            assertFalse(variantEntity.getAlternate().isEmpty());
            assertNotEquals(0, variantEntity.getStart());
            assertNotEquals(0, variantEntity.getEnd());
            assertEquals(MAIN_ID, variantEntity.getMainId());
        });
    }

    private void testGetVariantsByRegionHelper(String testRegion, int expectedVariants, HttpStatus status) throws
            URISyntaxException {
        List<Variant> results = regionWsHelper(testRegion, status, null);

        if (results == null) {
            assertEquals(0, expectedVariants);
            return;
        }

        assertEquals(expectedVariants, results.size());
        results.forEach(variantEntity -> {
            assertFalse(variantEntity.getChromosome().isEmpty());
            assertFalse(variantEntity.getReference().isEmpty());
            assertFalse(variantEntity.getAlternate().isEmpty());
            assertNotEquals(0, variantEntity.getStart());
            assertNotEquals(0, variantEntity.getEnd());
            assertEquals(MAIN_ID, variantEntity.getMainId());
        });
    }

    private List<Variant> regionWsHelper(String testRegion, HttpStatus status, ContigNamingConvention contigNamingConvention) {
        String url = "/v2/regions/" + testRegion + "/variants?species=mmusculus&assembly=grcm38";
        if (contigNamingConvention != null) {
            url += "&contigNamingConvention=" + contigNamingConvention;
        }
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
        return variantList;
    }

    @Test
    public void testGetVariantsByExistingRegions() throws URISyntaxException {
        testGetVariantsByRegionHelper("20:60000-61000,20:61500-62500", 2, HttpStatus.OK);
    }

    @Test
    public void testGetVariantsByNonExistingRegion() throws URISyntaxException {
        testGetVariantsByRegionHelper("21:8000-9000", 0, HttpStatus.NO_CONTENT);
    }

    @Test
    public void testGetVariantsByNonExistingRegions() throws URISyntaxException {
        testGetVariantsByRegionHelper("21:8000-9000,21:8000-9000", 0, HttpStatus.NO_CONTENT);
    }
}
