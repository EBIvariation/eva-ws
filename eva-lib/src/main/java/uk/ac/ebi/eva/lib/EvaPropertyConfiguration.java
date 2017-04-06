package uk.ac.ebi.eva.lib;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.ac.ebi.eva.lib.config.EvaProperty;

@EnableConfigurationProperties
@Configuration
public class EvaPropertyConfiguration {

    @Bean
    public EvaProperty evaProperty() {
        return new EvaProperty();
    }
}
