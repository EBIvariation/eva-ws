package uk.ac.ebi.eva.server.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import uk.ac.ebi.eva.lib.MongoConfiguration;
import uk.ac.ebi.eva.lib.MultiMongoFactoryConfiguration;

@Configuration
@EnableMongoRepositories("uk.ac.ebi.eva.lib.repository")
@PropertySource("classpath:eva.properties")
@Import({MongoConfiguration.class, MultiMongoFactoryConfiguration.class})
public class MongoEvaLibConfiguration {
}
