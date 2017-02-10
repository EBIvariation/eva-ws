package uk.ac.ebi.eva.lib.metadata;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import uk.ac.ebi.eva.lib.extension.ExtendedJpaRepositoryFunctionsImpl;

@SpringBootConfiguration
@ComponentScan
@EntityScan(basePackages = {"uk.ac.ebi.eva.lib.entity"})
@EnableJpaRepositories(basePackages = {"uk.ac.ebi.eva.lib.repository"}, repositoryBaseClass = ExtendedJpaRepositoryFunctionsImpl.class)
public class MetadataTestConfiguration {
}
