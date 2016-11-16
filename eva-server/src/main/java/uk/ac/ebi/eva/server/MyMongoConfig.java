package uk.ac.ebi.eva.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import uk.ac.ebi.eva.commons.models.converters.data.MongoDBObjectToVariantEntityConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Smith
 */
@Configuration
public class MyMongoConfig extends ApplicationConfiguration {

    @Bean
    public CustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();
        converters.add(new MongoDBObjectToVariantEntityConverter());
        return new CustomConversions(converters);
    }

    @Bean
    public MappingMongoConverter mongoConverter() throws IOException {
        MongoMappingContext mappingContext = new MongoMappingContext();
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory());
        MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mappingContext);
        mongoConverter.setCustomConversions(customConversions());
        mongoConverter.afterPropertiesSet();
        return mongoConverter;
    }

}
