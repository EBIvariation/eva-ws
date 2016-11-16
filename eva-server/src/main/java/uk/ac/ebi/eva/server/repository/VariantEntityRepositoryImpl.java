package uk.ac.ebi.eva.server.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

/**
 * @author Tom Smith
 */
public class VariantEntityRepositoryImpl {
    MongoDbFactory mongoDbFactory;
    MongoTemplate mongoTemplate;
    MappingMongoConverter mappingMongoConverter;

    @Autowired
    public VariantEntityRepositoryImpl(MongoDbFactory mongoDbFactory, MappingMongoConverter mappingMongoConverter) {
        this.mongoDbFactory = mongoDbFactory;
        this.mappingMongoConverter = mappingMongoConverter;
        mongoTemplate = new MongoTemplate(mongoDbFactory, mappingMongoConverter);
    }

//    private CustomConversions customConversions() {
//        List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();
//        converters.add(new MongoDBObjectToVariantConverter());
//        return new CustomConversions(converters);
//    }
//
//    private MappingMongoConverter mongoConverter() {
//        MongoMappingContext mappingContext = new MongoMappingContext();
//        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
//        MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mappingContext);
//        mongoConverter.setCustomConversions(customConversions());
//        mongoConverter.afterPropertiesSet();
//        return mongoConverter;
//    }
//
//    private MongoTemplate mongoTemplate() {
//        return new MongoTemplate(mongoDbFactory, mongoConverter());
//    }

}
