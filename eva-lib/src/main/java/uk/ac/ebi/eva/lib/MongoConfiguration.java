/*
 * Copyright 2016 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.lib;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import uk.ac.ebi.eva.commons.models.converters.data.DBObjectToVariantEntityConverter;
import uk.ac.ebi.eva.commons.models.converters.data.DbObjectToVariantGlobalStatsConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MongoDbFactory mongoDbFactory;

    @Value("${eva.mongo.collections.files}")
    private String mongoCollectionsFiles;

    @Value("${eva.mongo.collections.annotation_metadata}")
    private String mongoCollectionsAnnotationMetadata;

    @Bean
    public String mongoCollectionsFiles() {
        return mongoCollectionsFiles;
    }

    @Bean
    public String mongoCollectionsAnnotationMetadata() {
        return mongoCollectionsAnnotationMetadata;
    }

    @Bean
    public CustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new DBObjectToVariantEntityConverter());
        converters.add(new DbObjectToVariantGlobalStatsConverter());
        return new CustomConversions(converters);
    }

    @Bean
    public MongoMappingContext mongoMappingContext() {
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setApplicationContext(applicationContext);
        return mappingContext;
    }

    @Bean
    public MappingMongoConverter mappingMongoConverter() throws IOException {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
        MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext());

        mongoConverter.setCustomConversions(customConversions());
        mongoConverter.afterPropertiesSet();

        // TODO jmmut: see if this works if we want to exclude the _class
        //    converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return mongoConverter;
    }

}
