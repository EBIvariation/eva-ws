package uk.ac.ebi.eva.server.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

import uk.ac.ebi.eva.lib.MongoConfiguration;
import uk.ac.ebi.eva.lib.MultiMongoFactoryConfiguration;

@Configuration
@Import({MongoConfiguration.class, MultiMongoFactoryConfiguration.class})
public class MongoEvaLibConfiguration {

    @Bean
    public MongoTemplate mongoTemplate(@Autowired MongoDbFactory mongoDbFactory,
                                       @Autowired MappingMongoConverter mappingMongoConverter)
    {
        MongoTemplate template = new MongoTemplate(mongoDbFactory, mappingMongoConverter);
        return template;
    }
}
