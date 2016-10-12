/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2016 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDbFactory;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.ac.ebi.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.datastore.MultiMongoDbFactory;

import java.io.IOException;
import java.util.Properties;

@SpringBootApplication
@EnableSwagger2
@EntityScan(basePackages = {"uk.ac.ebi.eva.lib.spring.data.entity"})
@EnableJpaRepositories(basePackages = {"uk.ac.ebi.eva.lib.spring.data.repository"}, repositoryBaseClass = ExtendedJpaRepositoryFunctionsImpl.class)
@ComponentScan(basePackages = {"uk.ac.ebi.eva.lib.spring.data","uk.ac.ebi.variation.eva.server"})
public class Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

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

//    /**
//     * Create a Datasource bean with the connection to a jndi datasource.
//     *
//     * @return
//     * @throws IOException
//     * @throws NamingException
//     */
//    @Bean(name = "dataSource")
//    public DataSource evaProDataSource() throws IOException, NamingException {
//        Properties properties = new Properties();
//        properties.load(Application.class.getResourceAsStream("/eva.properties"));
//        String dsName = properties.getProperty("eva.evapro.datasource", "evapro");
//        JndiTemplate jndiTemplate = new JndiTemplate();
//        return (DataSource) jndiTemplate.lookup("java:/comp/env/jdbc/" + dsName);
//    }

    @Bean
    public Docket apiConfiguration() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .pathMapping("/")
                .select()
                .apis(RequestHandlerSelectors.basePackage("uk.ac.ebi.eva.server"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("European Variation Archive REST Web Services API")
                .contact(new Contact("the European Variation Archive team", "www.ebi.ac.uk/eva", "eva-helpdesk@ebi.ac.uk"))
                .license("Apache License Version 2.0")
                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
                .version("1.0")
                .build();
    }

}
