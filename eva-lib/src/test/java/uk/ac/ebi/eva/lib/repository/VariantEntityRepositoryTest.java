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
package uk.ac.ebi.eva.lib.repository;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantSourceEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.ebi.eva.commons.models.converters.data.DBObjectToVariantConverter;
import uk.ac.ebi.eva.commons.models.converters.data.DBObjectToVariantSourceEntryConverter;
import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;
import uk.ac.ebi.eva.lib.configuration.MongoRepositoryTestConfiguration;
import uk.ac.ebi.eva.lib.filter.FilterBuilder;
import uk.ac.ebi.eva.lib.filter.VariantEntityRepositoryFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for VariantEntityRepository
 * <p>
 * Uses in memory Mongo database spoof Fongo, and loading data from json using lordofthejars nosqlunit.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoRepositoryTestConfiguration.class})
@UsingDataSet(locations = {"/test-data/variants.json", "/test-data/files.json"})
public class VariantEntityRepositoryTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("test-db");

    @Autowired
    private VariantEntityRepository variantEntityRepository;

    @Test
    public void checkFieldPresence() throws IOException {

        List<Region> regions = new ArrayList<>();
        regions.add(new Region("11", 183000, 183300));

        List<VariantEntityRepositoryFilter> filters = new ArrayList<>();
        List<String> exclude = new ArrayList<>();

        List<VariantEntity> variantEntityList = variantEntityRepository
                .findByRegionsAndComplexFilters(regions, filters, exclude,
                                                new PageRequest(0, 100000000));

        for (VariantEntity currVariantEntity : variantEntityList) {
            assertFalse(currVariantEntity.getSourceEntries().isEmpty());
            assertFalse(currVariantEntity.getIds().isEmpty());
            for (Map.Entry<String, VariantSourceEntry> sourceEntryEntry :
                    currVariantEntity.getSourceEntries().entrySet()) {
                assertFalse(sourceEntryEntry.getValue().getAttributes().isEmpty());
            }
        }

    }

    @Test
    public void testVariantIdIsFound() {
        String id = "rs776523794";
        List<VariantEntityRepositoryFilter> filters = new ArrayList<>();
        List<String> exclude = new ArrayList<>();
        List<VariantEntity> variantEntityList = variantEntityRepository
                .findByIdsAndComplexFilters(id, filters, exclude, null);
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() > 0);
        Set<String> idSet = new HashSet<>();
        idSet.add(id);
        idSet.add("ss664037839");
        assertEquals(idSet, variantEntityList.get(0).getIds());
    }

    @Test
    public void testCountByIdsAndComplexFilters() {
        String id = "rs776523794";
        List<VariantEntityRepositoryFilter> filters = new ArrayList<>();
        Long count = variantEntityRepository.countByIdsAndComplexFilters(id, filters);
        assertEquals(new Long(1), count);
    }

    @Test
    public void testCountByIdsAndComplexFiltersZeroCount() {
        String id = "not_a_real_id";
        List<VariantEntityRepositoryFilter> filters = new ArrayList<>();
        Long count = variantEntityRepository.countByIdsAndComplexFilters(id, filters);
        assertEquals(new Long(0), count);
    }

    @Test
    public void testNonExistentVariantIdIsNotFound() {
        String id = "notarealid";
        List<VariantEntityRepositoryFilter> filters = new ArrayList<>();
        List<String> exclude = new ArrayList<>();
        List<VariantEntity> variantEntityList = variantEntityRepository
                .findByIdsAndComplexFilters(id, filters, exclude, null);
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() == 0);
    }

    @Test
    public void testVariantRegionIsFound() {
        String chr = "11";
        int start = 180002;
        int end = 180002;
        Region region = new Region(chr, start, end);
        List<VariantEntityRepositoryFilter> filters = new ArrayList<>();
        List<String> exclude = new ArrayList<>();
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<VariantEntity> variantEntityList =
                variantEntityRepository
                        .findByRegionsAndComplexFilters(regions, filters, exclude, new PageRequest(0, 1000000));
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() > 0);
        assertEquals(chr, variantEntityList.get(0).getChromosome());
        assertEquals(start, variantEntityList.get(0).getStart());
        assertEquals(end, variantEntityList.get(0).getStart());
    }

    @Test
    public void testVariantRegionIsFoundMultiple() {
        String chr = "11";
        int start = 185000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<VariantEntityRepositoryFilter> filters = new ArrayList<>();
        List<String> exclude = new ArrayList<>();
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<VariantEntity> variantEntityList =
                variantEntityRepository
                        .findByRegionsAndComplexFilters(regions, filters, exclude, new PageRequest(0, 1000000));
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() > 0);
        assertEquals(309, variantEntityList.size());
        VariantEntity prevVariantEntity = variantEntityList.get(0);
        for (VariantEntity currVariantEntity : variantEntityList) {
            assertTrue(prevVariantEntity.getStart() <= currVariantEntity.getStart());
        }
    }

    @Test
    public void testCountByRegionsAndComplexFilters() {
        String chr = "11";
        int start = 185000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<VariantEntityRepositoryFilter> filters = new ArrayList<>();
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        Long count = variantEntityRepository.countByRegionsAndComplexFilters(regions, filters);
        assertEquals(new Long(309), count);
    }

    @Test
    public void testNonExistentVariantRegionIsNotFound() {
        String chr = "11";
        int start = 61098;
        int end = 60916;
        Region region = new Region(chr, start, end);
        List<VariantEntityRepositoryFilter> filters = new ArrayList<>();
        List<String> exclude = new ArrayList<>();
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<VariantEntity> variantEntityList =
                variantEntityRepository
                        .findByRegionsAndComplexFilters(regions, filters, exclude, new PageRequest(0, 1000000));
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() == 0);
    }

    @Test
    public void testRegionIsFoundWithConsequenceType() {
        List<String> cts = new ArrayList<>();
        cts.add("SO:0001566");
        String chr = "11";
        int start = 180000;
        int end = 190000;
        List<VariantEntityRepositoryFilter> filters = new FilterBuilder().withConsequenceType(cts).build();
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<String> exclude = new ArrayList<>();
        testFiltersHelperRegion(regions, filters, exclude, 209);
    }

    @Test
    public void testRegionIsFoundWithMafGreaterThan() {
        String chr = "11";
        int start = 185000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<VariantEntityRepositoryFilter> filters = new FilterBuilder().withMaf(">0.125").build();
        List<String> exclude = new ArrayList<>();
        testFiltersHelperRegion(regions, filters, exclude, 43);
    }

    @Test
    public void testRegionIsFoundWithMafGreaterThanEquals() {
        String chr = "11";
        int start = 189000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<VariantEntityRepositoryFilter> filters = new FilterBuilder().withMaf(">=0.125").build();
        List<String> exclude = new ArrayList<>();
        testFiltersHelperRegion(regions, filters, exclude, 16);
    }

    @Test
    public void testRegionIsFoundWithMafEquals() {
        String chr = "11";
        int start = 185000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<VariantEntityRepositoryFilter> filters = new FilterBuilder().withMaf("=0.5").build();
        List<String> exclude = new ArrayList<>();
        testFiltersHelperRegion(regions, filters, exclude, 22);
    }

    @Test
    public void testRegionIsFoundWithPolyphenGreaterThan() {
        String chr = "11";
        int start = 190000;
        int end = 193719;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<VariantEntityRepositoryFilter> filters = new FilterBuilder().withPolyphenScore(">0.5").build();
        List<String> exclude = new ArrayList<>();
        testFiltersHelperRegion(regions, filters, exclude, 4);
    }

    @Test
    public void testRegionIsFoundWithSiftLessThan() {
        String chr = "11";
        int start = 190000;
        int end = 193719;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<VariantEntityRepositoryFilter> filters = new FilterBuilder().withSiftScore("<0.5").build();
        List<String> exclude = new ArrayList<>();
        testFiltersHelperRegion(regions, filters, exclude, 11);
    }

    @Test
    public void testRegionIsFoundWithStudies() {
        List<String> studies = new ArrayList<>();
        studies.add("PRJEB6930");
        String chr = "11";
        int start = 180000;
        int end = 180500;
        List<VariantEntityRepositoryFilter> filters = new FilterBuilder().withStudies(studies).build();
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<String> exclude = new ArrayList<>();
        testFiltersHelperRegion(regions, filters, exclude, 20);
    }

    @Test
    public void testRegionIsFoundWithFiles() {
        List<String> files = new ArrayList<>();
        files.add("ERZ019961");
        String chr = "11";
        int start = 180000;
        int end = 180500;
        List<VariantEntityRepositoryFilter> filters = new FilterBuilder().withFiles(files).build();
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<String> exclude = new ArrayList<>();
        testFiltersHelperRegion(regions, filters, exclude, 7);
    }

    @Test
    public void testRegionIsFoundWithTypes() {
        List<Variant.VariantType> types = new ArrayList<>();
        types.add(Variant.VariantType.INDEL);
        String chr = "11";
        int start = 180000;
        int end = 180500;
        List<VariantEntityRepositoryFilter> filters = new FilterBuilder().withVariantTypes(types).build();
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<String> exclude = new ArrayList<>();
        testFiltersHelperRegion(regions, filters, exclude, 3);
    }

    @Test
    public void testRegionIsFoundWithAlternates() {
        List<String> alternates = new ArrayList<>();
        alternates.add("T");
        String chr = "11";
        int start = 180000;
        int end = 180500;
        List<VariantEntityRepositoryFilter> filters = new FilterBuilder().withAlternates(alternates).build();
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<String> exclude = new ArrayList<>();
        testFiltersHelperRegion(regions, filters, exclude, 21);
    }

    @Test
    public void testFindByRegionsAndComplexFilters() {

        List<Region> regions = new ArrayList<>();
        regions.add(new Region("11", 183000, 183300));
        regions.add(new Region("11", 180100, 180200));
        regions.add(new Region("11", 190000, 190200));

        List<VariantEntityRepositoryFilter> filters = new ArrayList<>();
        List<String> exclude = new ArrayList<>();

        testFindByRegionsAndComplexFiltersHelper(regions, filters, exclude, 28);

        regions = new ArrayList<>();
        regions.add(new Region("11", 180001, 180079)); //4

        testFindByRegionsAndComplexFiltersHelper(regions, filters, null, 4);

        regions.add(new Region("11", 180150, 180180)); //5

        testFindByRegionsAndComplexFiltersHelper(regions, filters, null, 9);

        regions.add(new Region("11", 180205, 180221)); //2

        testFindByRegionsAndComplexFiltersHelper(regions, filters, null, 11);
    }

    @Test
    public void testFindByRegionsAndComplexFiltersExcludeSingleRoot() {
        List<Region> regions = new ArrayList<>();
        regions.add(new Region("11", 183000, 183300));

        List<String> exclude = new ArrayList<>();
        exclude.add(DBObjectToVariantConverter.FILES_FIELD);
        List<VariantEntityRepositoryFilter> filters = new ArrayList<>();

        List<VariantEntity> variantEntityList =
                variantEntityRepository
                        .findByRegionsAndComplexFilters(regions, filters, exclude, new PageRequest(0, 10000));
        assertNotNull(variantEntityList);
        for (VariantEntity currVariantEntity : variantEntityList) {
            assertTrue(currVariantEntity.getSourceEntries().isEmpty());
        }
    }

    @Test
    public void testFindByRegionsAndComplexFiltersExcludeAttributes() {
        List<Region> regions = new ArrayList<>();
        regions.add(new Region("11", 183000, 183300));

        List<String> exclude = new ArrayList<>();
        exclude.add(
                DBObjectToVariantConverter.FILES_FIELD + "." + DBObjectToVariantSourceEntryConverter.ATTRIBUTES_FIELD);
        List<VariantEntityRepositoryFilter> filters = new ArrayList<>();

        List<VariantEntity> variantEntityList =
                variantEntityRepository
                        .findByRegionsAndComplexFilters(regions, filters, exclude, new PageRequest(0, 10000));
        assertNotNull(variantEntityList);
        for (VariantEntity currVariantEntity : variantEntityList) {
            for (VariantSourceEntry variantSourceEntry : currVariantEntity.getSourceEntries().values()) {
                assertFalse(variantSourceEntry.getFileId().isEmpty());
                assertTrue(variantSourceEntry.getAttributes().isEmpty());
            }
        }
    }

    @Test
    public void testFindDistinctChromosomesByStudyId() {
        Set<String> chromosomeSet = variantEntityRepository.findDistinctChromosomes();

        Set<String> expectedChromosomeSet = new HashSet<>();
        expectedChromosomeSet.add("11");
        expectedChromosomeSet.add("9");
        expectedChromosomeSet.add("2");

        assertEquals(expectedChromosomeSet, new HashSet<>(chromosomeSet));
    }

    @Test
    public void testCountByChromosomeAndStartAndEndAndAltAndStudy() {
        List<String> studies = new ArrayList<>();
        studies.add("PRJEB5829");
        Long count = (long) variantEntityRepository.findByChromosomeAndStartAndAltAndStudyIn("11", 180002, "A", studies)
                                                   .size();
        assertEquals(new Long(1), count);
    }

    @Test
    public void testCountByChromosomeAndStartAndTypeAndStudy() {
        List<String> studies = new ArrayList<>();
        studies.add("PRJX00001");
        Long count = (long) variantEntityRepository.findByChromosomeAndStartAndTypeAndStudyIn("11", 180077,
                                                                                              Variant.VariantType.INDEL,
                                                                                              studies)
                                                   .size();
        assertEquals(new Long(1), count);
    }

    @Test
    public void testSamplesData() {
        String chr = "11";
        int start = 180001;
        int end = 180003;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);

        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, null, null, new PageRequest(0, 10000));

        assertEquals(1, variantEntityList.size());
        for (VariantSourceEntry variantSourceEntry : variantEntityList.get(0).getSourceEntries().values()) {
            if (!variantSourceEntry.getFileId().equals("ERZ015345")) {
                continue;
            }
            assertNotEquals(0, variantSourceEntry.getSamplesData().size());
            Map<String, Map<String, String>> samplesData = variantSourceEntry.getSamplesData();
            assertEquals("1|1", samplesData.get("HG00096").get("GT"));
        }
    }

    private void testFiltersHelperRegion(List<Region> regions, List<VariantEntityRepositoryFilter> filters,
                                         List<String> exclude, int expectedResultLength) {
        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, filters, exclude,
                                                                       new PageRequest(0, 10000));
        assertNotNull(variantEntityList);
        assertEquals(expectedResultLength, variantEntityList.size());
    }

    private void testFindByRegionsAndComplexFiltersHelper(List<Region> regions,
                                                          List<VariantEntityRepositoryFilter> filters,
                                                          List<String> exclude, int expectedResultLength) {
        List<VariantEntity> variantEntityList =
                variantEntityRepository
                        .findByRegionsAndComplexFilters(regions, filters, exclude, new PageRequest(0, 100000000));
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() > 0);
        VariantEntity prevVariantEntity = variantEntityList.get(0);
        for (VariantEntity currVariantEntity : variantEntityList) {
            if (prevVariantEntity.getChromosome().equals(currVariantEntity.getChromosome())) {
                assertTrue(prevVariantEntity.getStart() <= currVariantEntity.getStart());
            }
        }
        assertEquals(expectedResultLength, variantEntityList.size());
    }
}
