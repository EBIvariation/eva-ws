package uk.ac.ebi.eva.server.repository;

import com.github.fakemongo.Fongo;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@UsingDataSet(locations = {"/testData/variants.json"})
public class VariantEntityRepositoryTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("demo-test");

    @Autowired
    private VariantEntityRepository variantEntityRepository;

    @Test
    public void shouldFindByVariantId(){
        String id = "rs527639301";
        final List<VariantEntity> variantEntityList = variantEntityRepository.findByIds(id);
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() > 0);
        Set<String> idSet = new HashSet<>();
        idSet.add(id);
        assertEquals(idSet, variantEntityList.get(0).getIds());
    }

    @Test
    public void shouldFindByVariantIdNonExistent(){
        String id = "notarealid";
        final List<VariantEntity> variantEntityList = variantEntityRepository.findByIds(id);
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() == 0);
    }

    @Test
    public void shouldFindByVariantRegion(){
        String chr = "20";
        int start = 60343;
        int end = 60343;
        final List<VariantEntity> variantEntityList = variantEntityRepository.findByChrAndStartWithMarginAndEndWithMargin(chr, start, end);
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() > 0);
        assertEquals(chr, variantEntityList.get(0).getChromosome());
        assertEquals(start, variantEntityList.get(0).getStart());
        assertEquals(end, variantEntityList.get(0).getStart());
    }

    @Test
    public void shouldFindByVariantRegionMultiple(){
        String chr = "20";
        int start = 60916;
        int end = 61098;
        final List<VariantEntity> variantEntityList = variantEntityRepository.findByChrAndStartWithMarginAndEndWithMargin(chr, start, end);
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() > 0);
        assertEquals(4, variantEntityList.size());
        VariantEntity prevVariantEntity = variantEntityList.get(0);
        for (VariantEntity currVariantEntity : variantEntityList) {
            assertTrue(prevVariantEntity.getStart() <= currVariantEntity.getStart());
        }
    }

    @Test
    public void shouldFindByVariantRegionNonExistent(){
        String chr = "20";
        int start = 61098;
        int end = 60916;
        final List<VariantEntity> variantEntityList = variantEntityRepository.findByChrAndStartWithMarginAndEndWithMargin(chr, start, end);
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() == 0);
    }


    @Configuration
    @EnableMongoRepositories
    @ComponentScan(basePackageClasses = { VariantEntityRepository.class })
    static class VariantEntityRepositoryConfiguration extends AbstractMongoConfiguration {

        @Override
        protected String getDatabaseName() {
            return "demo-test";
        }

        @Bean
        public Mongo mongo() {
            return new Fongo("something").getMongo();
        }

        @Override
        protected String getMappingBasePackage() {
            return "uk.ac.ebi.eva.server.repository";
        }

        @Autowired
        private MongoDbFactory mongoDbFactory;

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