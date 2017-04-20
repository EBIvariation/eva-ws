package uk.ac.ebi.eva.lib.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({EvaProperties.class})
public class EvaPropertiesTestConfiguration {

    @Bean
    public EvaProperties evaProperties() {
        return new EvaProperties();
    }
}
