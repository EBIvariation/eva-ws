package uk.ac.ebi.eva.server.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.ac.ebi.eva.lib.extension.ExtendedJpaRepositoryFunctionsImpl;

@Configuration
@EnableJpaRepositories(basePackages = {"uk.ac.ebi.eva.lib.repositories"},
        repositoryBaseClass = ExtendedJpaRepositoryFunctionsImpl.class)
@EntityScan(basePackages = {"uk.ac.ebi.eva.lib.entities"})
@ComponentScan(basePackages = {"uk.ac.ebi.eva.lib.metadata"})
public class JpaRepositoryConfiguration {
}
