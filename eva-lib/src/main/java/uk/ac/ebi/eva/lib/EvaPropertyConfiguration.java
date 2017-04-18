package uk.ac.ebi.eva.lib;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import uk.ac.ebi.eva.lib.config.EvaProperty;

@EnableConfigurationProperties
@Configuration
@Import(EvaProperty.class)
public class EvaPropertyConfiguration {

    @Bean
    public EvaProperty evaProperty() {
        return new EvaProperty();
    }
}
