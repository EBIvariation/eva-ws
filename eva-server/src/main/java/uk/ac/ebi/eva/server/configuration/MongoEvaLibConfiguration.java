package uk.ac.ebi.eva.server.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import uk.ac.ebi.eva.lib.MongoConfiguration;
import uk.ac.ebi.eva.lib.MultiMongoFactoryConfiguration;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;

import java.io.IOException;

@Configuration
@EnableMongoRepositories("uk.ac.ebi.eva.lib.repository")
@Import({MongoConfiguration.class, MultiMongoFactoryConfiguration.class})
public class MongoEvaLibConfiguration {

    @Bean
    DBAdaptorConnector dbAdaptorConnector() throws IOException {
        return new DBAdaptorConnector();
    }
}
