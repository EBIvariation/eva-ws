/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
import com.mongodb.Mongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import uk.ac.ebi.eva.commons.models.converters.data.DBObjectToVariantEntityConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableMongoRepositories
@ComponentScan(basePackages = { "uk.ac.ebi.eva.lib.repository" })
public class RepositoryConfiguration extends AbstractMongoConfiguration {

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
        converters.add(new DBObjectToVariantEntityConverter());
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
