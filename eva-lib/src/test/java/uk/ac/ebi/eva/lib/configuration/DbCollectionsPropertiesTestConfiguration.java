package uk.ac.ebi.eva.lib.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({DbCollectionsProperties.class})
public class DbCollectionsPropertiesTestConfiguration {

    @Bean
    public DbCollectionsProperties dbCollectionsProperties() {
        return new DbCollectionsProperties();
    }
}
