package uk.ac.ebi.eva.lib.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SpringDataMongoDbProperties.class})
public class SpringDataMongoDbPropertiesTestConfiguration {
}
