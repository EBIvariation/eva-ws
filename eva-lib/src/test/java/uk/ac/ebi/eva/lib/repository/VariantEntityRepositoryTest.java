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

import com.github.fakemongo.Fongo;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.mongodb.Mongo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencb.biodata.models.feature.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.ebi.eva.commons.models.converters.data.MongoDBObjectToVariantEntityConverter;
import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for VariantEntityRepository
 *
 * Uses in memory Mongo database spoof Fongo, and loading data from json using lordofthejars nosqlunit.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@UsingDataSet(locations = {"/test-data/variants.json"})
public class VariantEntityRepositoryTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("test-db");

    @Autowired
    private VariantEntityRepository variantEntityRepository;

    @Test
    public void testVariantIdIsFound(){
        String id = "rs776523794";
        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByIdsAndComplexFilters(id, null, null, null, null, null, null, null, null,
                                                                   null);
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() > 0);
        Set<String> idSet = new HashSet<>();
        idSet.add(id);
        idSet.add("ss664037839");
        assertEquals(idSet, variantEntityList.get(0).getIds());
    }

    @Test
    public void testNonExistentVariantIdIsNotFound(){
        String id = "notarealid";
        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByIdsAndComplexFilters(id, null, null, null, null, null, null, null, null,
                                                                   null);
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() == 0);
    }

    @Test
    public void testVariantRegionIsFound(){
        String chr = "11";
        int start = 180002;
        int end = 180002;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, new ArrayList<>(), new ArrayList<>(), null, null,
                                                                      null, null, null, null, new PageRequest(0, 1000000));
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() > 0);
        assertEquals(chr, variantEntityList.get(0).getChromosome());
        assertEquals(start, variantEntityList.get(0).getStart());
        assertEquals(end, variantEntityList.get(0).getStart());
    }

    @Test
    public void testVariantRegionIsFoundMultiple(){
        String chr = "11";
        int start = 185000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, new ArrayList<>(), new ArrayList<>(), null, null,
                                                                      null, null, null, null, new PageRequest(0, 1000000));
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() > 0);
        assertEquals(309, variantEntityList.size());
        VariantEntity prevVariantEntity = variantEntityList.get(0);
        for (VariantEntity currVariantEntity : variantEntityList) {
            assertTrue(prevVariantEntity.getStart() <= currVariantEntity.getStart());
        }
    }

    @Test
    public void testNonExistentVariantRegionIsNotFound(){
        String chr = "11";
        int start = 61098;
        int end = 60916;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, new ArrayList<>(), new ArrayList<>(), null, null,
                                                                      null, null, null, null, new PageRequest(0, 1000000));
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() == 0);
    }

    @Test
    public void testRegionIsFoundWithConsequenceType() {
        List<String> cts = new ArrayList<>();
        cts.add("SO:0001627");
        String chr = "11";
        int start = 188000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        testFiltersHelperRegion(regions, new ArrayList<>(), cts, null, null, null, null, null, null, 94);
    }

    @Test
    public void testRegionIsFoundWithMafGreaterThan() {
        String chr = "11";
        int start = 185000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        testFiltersHelperRegion(regions, new ArrayList<>(), new ArrayList<>(), VariantEntityRepository.RelationalOperator.GT, 0.125,
                                null, null, null, null, 37);
    }

    @Test
    public void testRegionIsFoundWithMafGreaterThanEquals() {
        String chr = "11";
        int start = 189000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        testFiltersHelperRegion(regions, new ArrayList<>(), new ArrayList<>(), VariantEntityRepository.RelationalOperator.GTE,
                                0.125, null, null, null, null, 15);
    }

    @Test
    public void testRegionIsFoundWithMafEquals() {
        String chr = "11";
        int start = 185000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        testFiltersHelperRegion(regions, new ArrayList<>(), new ArrayList<>(), VariantEntityRepository.RelationalOperator.EQ, 0.5,
                                null, null, null, null, 8);
    }

    @Test
    public void testRegionIsFoundWithPolyphenGreaterThan() {
        String chr = "11";
        int start = 190000;
        int end = 193719;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        testFiltersHelperRegion(regions, new ArrayList<>(), new ArrayList<>(), null, null,
                                VariantEntityRepository.RelationalOperator.GT, 0.5, null, null, 4);
    }

    @Test
    public void testRegionIsFoundWithSiftLessThan() {
        String chr = "11";
        int start = 190000;
        int end = 193719;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        testFiltersHelperRegion(regions, new ArrayList<>(), new ArrayList<>(), null, null, null, null,
                                VariantEntityRepository.RelationalOperator.LT, 0.5, 11);
    }

    @Test
    public void testRegionIsFoundWithStudies() {
        List<String> studies = new ArrayList<>();
        studies.add("PRJEB6930");
        String chr = "11";
        int start = 190000;
        int end = 191000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        testFiltersHelperRegion(regions, studies, new ArrayList<>(), null, null, null, null, null, null, 14);
    }

    @Test
    public void testFindByRegionsAndComplexFilters() {

        List<Region> regions = new ArrayList<>();
        regions.add(new Region("11", 183000, 183300));
        regions.add(new Region("11", 180100, 180200));
        regions.add(new Region("11", 190000, 190200));

        testFindByRegionsAndComplexFiltersHelper(regions, null, null, VariantEntityRepository.RelationalOperator.NONE,
                                                 null, VariantEntityRepository.RelationalOperator.NONE, null,
                                                 VariantEntityRepository.RelationalOperator.NONE, null, 28);

        regions = new ArrayList<>();
        regions.add(new Region("11", 180001, 180079)); //4

        testFindByRegionsAndComplexFiltersHelper(regions, null, null, VariantEntityRepository.RelationalOperator.NONE,
                                                 null, VariantEntityRepository.RelationalOperator.NONE, null,
                                                 VariantEntityRepository.RelationalOperator.NONE, null, 4);

        regions.add(new Region("11", 180150, 180180)); //5

        testFindByRegionsAndComplexFiltersHelper(regions, null, null, VariantEntityRepository.RelationalOperator.NONE,
                                                 null, VariantEntityRepository.RelationalOperator.NONE, null,
                                                 VariantEntityRepository.RelationalOperator.NONE, null, 9);

        regions.add(new Region("11", 180205, 180221)); //2

        testFindByRegionsAndComplexFiltersHelper(regions, null, null, VariantEntityRepository.RelationalOperator.NONE,
                                                 null, VariantEntityRepository.RelationalOperator.NONE, null,
                                                 VariantEntityRepository.RelationalOperator.NONE, null, 11);
    }

    private void testFiltersHelperRegion(List<Region> regions, List<String> studies, List<String> consequenceType,
                                         VariantEntityRepository.RelationalOperator mafOperator, Double mafValue,
                                         VariantEntityRepository.RelationalOperator polyphenOperator, Double polyphenValue,
                                         VariantEntityRepository.RelationalOperator siftOperator, Double siftValue,
                                         int expectedResultLength) {
        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, studies, consequenceType, mafOperator,
                                                                      mafValue, polyphenOperator, polyphenValue,
                                                                      siftOperator, siftValue, new PageRequest(0, 10000));
        assertNotNull(variantEntityList);
        assertEquals(expectedResultLength, variantEntityList.size());
    }

    private void testFindByRegionsAndComplexFiltersHelper(List<Region> regions, List<String> studies,
                                                          List<String> consequenceType,
                                                          VariantEntityRepository.RelationalOperator mafOperator,
                                                          Double mafValue,
                                                          VariantEntityRepository.RelationalOperator polyphenOperator,
                                                          Double polyphenValue,
                                                          VariantEntityRepository.RelationalOperator siftOperator,
                                                          Double siftValue,
                                                          int expectedResultLength) {
        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, new ArrayList<>(), new ArrayList<>(),
                                                                       null, null, null, null, null, null,
                                                                       new PageRequest(0, 100000000));
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

    @Configuration
    @EnableMongoRepositories
    @ComponentScan(basePackageClasses = { VariantEntityRepository.class })
    static class VariantEntityRepositoryConfiguration extends AbstractMongoConfiguration {

        @Autowired
        private MongoDbFactory mongoDbFactory;

        @Override
        protected String getDatabaseName() {
            return "test-db";
        }

        @Bean
        public Mongo mongo() {
            return new Fongo("defaultInstance").getMongo();
        }

        @Override
        protected String getMappingBasePackage() {
            return "uk.ac.ebi.eva.lib.repository";
        }

        @Bean
        public CustomConversions customConversions() {
            List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();
            converters.add(new MongoDBObjectToVariantEntityConverter());
            return new CustomConversions(converters);
        }

        @Bean
        public MappingMongoConverter mongoConverter() throws IOException {
            MongoMappingContext mappingContext = new MongoMappingContext();
            DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
            MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mappingContext);
            mongoConverter.setCustomConversions(customConversions());
            mongoConverter.afterPropertiesSet();
            return mongoConverter;
        }
    }
}