package uk.ac.ebi.eva.server.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"uk.ac.ebi.eva.lib.config"})
public class EvaPropertyConfiguration {
}
