//package uk.ac.ebi.eva.server.repository;
//
//import com.github.fakemongo.Fongo;
//import com.mongodb.Mongo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.convert.converter.Converter;
//import org.springframework.data.mongodb.MongoDbFactory;
//import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
//import org.springframework.data.mongodb.core.convert.CustomConversions;
//import org.springframework.data.mongodb.core.convert.DbRefResolver;
//import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
//import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
//import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
//import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
//import org.springframework.stereotype.Component;
//
//import uk.ac.ebi.eva.commons.models.converters.data.MongoDBObjectToVariantEntityConverter;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//@Configuration
//@EnableMongoRepositories
//@ComponentScan(basePackageClasses = { VariantEntityRepository.class, VariantEntityRepositoryImpl.class })
//public class VariantEntityRepositoryConfiguration extends AbstractMongoConfiguration {
//    @Autowired
//    private MongoDbFactory mongoDbFactory;
//
//    @Override
//    protected String getDatabaseName() {
//        return "test-db";
//    }
//
//    @Bean
//    public Mongo mongo() {
//        return new Fongo("defaultInstance").getMongo();
//    }
//
//    @Override
//    protected String getMappingBasePackage() {
//        return "uk.ac.ebi.eva.server.repository";
//    }
//
//    @Bean
//    public CustomConversions customConversions() {
//        List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();
//        converters.add(new MongoDBObjectToVariantEntityConverter());
//        return new CustomConversions(converters);
//    }
//
//    @Bean
//    public MappingMongoConverter mongoConverter() throws IOException {
//        MongoMappingContext mappingContext = new MongoMappingContext();
//        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
//        MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mappingContext);
//        mongoConverter.setCustomConversions(customConversions());
//        mongoConverter.afterPropertiesSet();
//        return mongoConverter;
//    }
//}
