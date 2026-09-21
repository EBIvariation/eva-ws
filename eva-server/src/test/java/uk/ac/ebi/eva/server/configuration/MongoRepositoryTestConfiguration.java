/*
 * Copyright 2016-2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.server.configuration;

import com.mongodb.client.MongoClient;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import uk.ac.ebi.eva.lib.MongoConfiguration;
import uk.ac.ebi.eva.lib.Profiles;
import uk.ac.ebi.eva.lib.configuration.SpringDataMongoDbProperties;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;

import static uk.ac.ebi.eva.server.utils.MongoTestContainerHelper.mongo;

@Configuration
@Import({MongoConfiguration.class})
@PropertySource({"classpath:application.properties"})
@EnableMongoAuditing
@AutoConfigureDataMongo
public class MongoRepositoryTestConfiguration {

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", () -> mongo.getHost() + ":" + mongo.getFirstMappedPort());
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory,
                                       MappingMongoConverter mappingMongoConverter) {
        return new MongoTemplate(mongoDbFactory, mappingMongoConverter);
    }

    @Bean
    public MongoClient mongoClient(SpringDataMongoDbProperties properties) throws Exception {
        return DBAdaptorConnector.getMongoClient(properties);
    }

    @Bean
    @Profile(Profiles.TEST_MONGO_FACTORY)
    public MongoDatabaseFactory mongoDbFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, this.getDatabaseName());
    }

    private String getDatabaseName() {
        return "test-db";
    }
}
