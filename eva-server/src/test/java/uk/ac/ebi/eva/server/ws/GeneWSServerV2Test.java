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
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigAliasChromosome;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigNamingConvention;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.FeatureService;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.utils.TaxonomyUtils;
import uk.ac.ebi.eva.server.ws.contigalias.ContigAliasService;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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

    @MockBean
    private ContigAliasService contigAliasService;

    @MockBean
    private TaxonomyUtils taxonomyUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private String GENE_ID1 = "ENSG00000227232";

    private String GENE_ID2 = "ENSG00000227244";

    VariantWithSamplesAndAnnotation variantEntity = new VariantWithSamplesAndAnnotation("20", 1000, 1005,
            "A", "C", MAIN_ID);

    @Before
    public void setUp() throws Exception {
        List<Region> oneRegion = Collections.singletonList(new Region("20", 60000L, 62000L));
        given(variantService.findByRegionsAndComplexFilters(eq(oneRegion), any(), any(), any(), any()))
                .willReturn(Collections.singletonList(variantEntity));
        given(variantService.countByRegionsAndComplexFilters(eq(oneRegion), any())).willReturn(1L);

        List<Region> twoRegions = Arrays.asList(new Region("20", 60000L, 62000L),
                new Region("20", 63000L, 64000L));


        given(variantService.findByRegionsAndComplexFilters(eq(twoRegions), any(), any(), any(), any()))
                .willReturn(Arrays.asList(variantEntity, variantEntity));
        given(variantService.countByRegionsAndComplexFilters(eq(twoRegions), any())).willReturn(2L);

        given(variantService
                .findByRegionsAndComplexFilters(not(or(eq(oneRegion), eq(twoRegions))), any(), any(), any(), any()))
                .willReturn(Collections.emptyList());
        given(variantService.countByRegionsAndComplexFilters(not(or(eq(oneRegion), eq(twoRegions))), any()))
                .willReturn(0L);

        FeatureCoordinates feature1 = new FeatureCoordinates(GENE_ID1, "id", "feature", "20", 60000L, 62000L);
        FeatureCoordinates feature2 = new FeatureCoordinates(GENE_ID1, "id", "feature", "20", 63000L, 64000L);

        List<FeatureCoordinates> featureCoordinate = Arrays.asList(feature1);
        List<String> geneId = Arrays.asList(GENE_ID1);
        given(featureService.findAllByGeneIdsOrGeneNames(eq(geneId), eq(geneId))).willReturn(featureCoordinate);

        List<FeatureCoordinates> featureCoordinates = Arrays.asList(feature1, feature2);
        List<String> geneIds = Arrays.asList(GENE_ID1, GENE_ID2);
        given(featureService.findAllByGeneIdsOrGeneNames(eq(geneIds), eq(geneIds))).willReturn(featureCoordinates);

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
    public void testGetVariantsByExistingGene() throws URISyntaxException {
        Variant variant = testGetVariantsByGeneHelper("ENSG00000227232", 1, HttpStatus.OK, null).get(0);
        assertEquals("20", variant.getChromosome());
        assertEquals("A", variant.getReference());
        assertEquals("C", variant.getAlternate());
    }

    @Test
    public void testGetVariantsByExistingGeneWithTranslatedContig() throws Exception {
        List<Region> oneRegion = Collections.singletonList(new Region("20", 60000L, 62000L));
        given(variantService.countByRegionsAndComplexFilters(eq(oneRegion), any())).willReturn(1l);
        VariantWithSamplesAndAnnotation variant = new VariantWithSamplesAndAnnotation("20", 61000, 61005,
                "A", "C", MAIN_ID);
        List<Region> translatedRegion = Collections.singletonList(new Region("20", 60000L, 62000L));
        given(variantService.findByRegionsAndComplexFilters(eq(translatedRegion), any(), any(), any(), any()))
                .willReturn(Collections.singletonList(variant));

        ContigAliasChromosome contigAliasChromosome = new ContigAliasChromosome();
        contigAliasChromosome.setInsdcAccession("20");
        contigAliasChromosome.setEnaSequenceName("chr20");
        given(contigAliasService.getUniqueInsdcChromosomeByName("20", "GCA_000001635.2",
                ContigNamingConvention.ENA_SEQUENCE_NAME)).willReturn(contigAliasChromosome);
        given(contigAliasService.getMatchingContigNamingConvention(contigAliasChromosome, "1"))
                .willReturn(ContigNamingConvention.ENA_SEQUENCE_NAME);

        Variant variantRes = testGetVariantsByGeneHelper("ENSG00000227232", 1, HttpStatus.OK,
                ContigNamingConvention.ENA_SEQUENCE_NAME).get(0);
        assertEquals("chr20", variantRes.getChromosome());
        assertEquals("A", variantRes.getReference());
        assertEquals("C", variantRes.getAlternate());
    }

    private List<Variant> testGetVariantsByGeneHelper(String testRegion, int expectedVariants, HttpStatus status,
                                                      ContigNamingConvention contigNamingConvention)
            throws URISyntaxException {
        String url = "/v2/genes/" + testRegion + "/variants?species=mmusculus&assembly=grcm38";
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

        if (variantList == null) {
            assertEquals(expectedVariants, 0);
            return null;
        }
        assertEquals(expectedVariants, variantList.size());
        return variantList;
    }

    @Test
    public void testGetVariantsByExistingGenes() throws URISyntaxException {
        List<Variant> variants = testGetVariantsByGeneHelper("ENSG00000227232,ENSG00000227244", 2,
                HttpStatus.OK, null);
        assertEquals("20", variants.get(0).getChromosome());
        assertEquals("A", variants.get(0).getReference());
        assertEquals("C", variants.get(0).getAlternate());
        assertEquals("20", variants.get(1).getChromosome());
        assertEquals("A", variants.get(1).getReference());
        assertEquals("C", variants.get(1).getAlternate());
    }

    @Test
    public void testGetVariantsByNonExistingGene() throws URISyntaxException {
        testGetVariantsByGeneHelper("nonexisting", 0, HttpStatus.NO_CONTENT, null);
    }

    @Test
    public void testGetVariantsByNonExistingGenes() throws URISyntaxException {
        testGetVariantsByGeneHelper("nonexisting,nonexisting", 0, HttpStatus.NO_CONTENT, null);
    }
}
