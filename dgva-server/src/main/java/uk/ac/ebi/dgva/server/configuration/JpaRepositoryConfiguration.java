/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.dgva.server.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.ac.ebi.eva.lib.extension.ExtendedJpaRepositoryFunctionsImpl;

@Configuration
@EnableJpaRepositories(basePackages = {"uk.ac.ebi.eva.lib.repositories"},
        repositoryBaseClass = ExtendedJpaRepositoryFunctionsImpl.class)
@EntityScan(basePackages = {"uk.ac.ebi.eva.lib.entity"})
@ComponentScan(basePackages = {"uk.ac.ebi.eva.lib.metadata.dgva", "uk.ac.ebi.eva.lib.metadata.shared"})
public class JpaRepositoryConfiguration {
}
