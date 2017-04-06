package uk.ac.ebi.eva.lib.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import uk.ac.ebi.eva.lib.config.EvaProperty;

@Configuration
@Import({EvaProperty.class})
public class EvaPropertyTestConfiguration {

    @Bean
    public EvaProperty evaProperty() {
        return new EvaProperty();
    }
}
