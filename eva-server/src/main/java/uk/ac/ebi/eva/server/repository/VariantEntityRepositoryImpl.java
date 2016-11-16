package uk.ac.ebi.eva.server.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

/**
 * @author Tom Smith
 */
public class VariantEntityRepositoryImpl {
    private MongoDbFactory mongoDbFactory;
    private MongoTemplate mongoTemplate;
    private MappingMongoConverter mappingMongoConverter;

    @Autowired
    public VariantEntityRepositoryImpl(MongoDbFactory mongoDbFactory, MappingMongoConverter mappingMongoConverter) {
        this.mongoDbFactory = mongoDbFactory;
        this.mappingMongoConverter = mappingMongoConverter;
        mongoTemplate = new MongoTemplate(mongoDbFactory, mappingMongoConverter);
    }

}
