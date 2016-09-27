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
package uk.ac.ebi.eva.server;

import com.mongodb.MongoClient;

import uk.ac.ebi.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.datastore.MultiMongoDbFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Jose Miguel Mut Lopez &lt;jmmut@ebi.ac.uk&gt;
 */
@Configuration
public class ApplicationConfiguration {
    /**
     * This factory will allow to use the FeatureRepository with several databases, as we are providing a
     * MultiMongoDbFactory as the implementation of MongoFactory to inject into the FeatureRepository.
     * @return MongoDbFactory
     * @throws IOException
     */
    @Bean
    public MongoDbFactory mongoDbFactory() throws IOException {
        Properties properties = new Properties();
        properties.load(Application.class.getResourceAsStream("/eva.properties"));
        MongoClient mongoClient = DBAdaptorConnector.getMongoClient(properties);
        return new MultiMongoDbFactory(mongoClient, "test");
    }
}
