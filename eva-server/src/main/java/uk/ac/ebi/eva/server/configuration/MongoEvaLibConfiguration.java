package uk.ac.ebi.eva.server.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import uk.ac.ebi.eva.lib.MongoConfiguration;

@Configuration
@EnableMongoRepositories("uk.ac.ebi.eva.lib.repository")
@Import(MongoConfiguration.class)
public class MongoEvaLibConfiguration {
}
