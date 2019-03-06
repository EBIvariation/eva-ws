/*
 * Copyright 2019 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.data.rest.webmvc.spi.BackendIdConverter;

import uk.ac.ebi.eva.server.models.ProgressReport;
import uk.ac.ebi.eva.server.models.ProgressReportPK;
import uk.ac.ebi.eva.server.repositories.ProgressReportRepository;

import java.io.Serializable;

@Configuration
public class ApplicationConfiguration extends RepositoryRestConfigurerAdapter {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.withEntityLookup().forRepository(ProgressReportRepository.class, (ProgressReport ed) -> {
            ProgressReportPK pk = new ProgressReportPK();
            pk.setDatabaseName(ed.getDatabaseName());
            pk.setGenbankAssemblyAccession(ed.getGenbankAssemblyAccession());
            return pk;
        }, ProgressReportRepository::findOne);
    }

    @Bean
    public BackendIdConverter progressReportPkConverter() {
        System.out.println("+++ Injecting ProgressReportPK converter");

        return new BackendIdConverter() {

            @Override
            public boolean supports(Class<?> delimiter) {
                return ProgressReportPK.class.equals(delimiter);
            }

            @Override
            public String toRequestId(Serializable id, Class<?> entityType) {
                ProgressReportPK pk = (ProgressReportPK) id;
                return String.format("%s,%s", pk.getDatabaseName(), pk.getGenbankAssemblyAccession());
            }

            @Override
            public Serializable fromRequestId(String id, Class<?> entityType) {
                if (id == null){
                    return null;
                }

                String[] parts = id.split(",");
                ProgressReportPK pk = new ProgressReportPK();
                pk.setDatabaseName(parts[0]);
                pk.setGenbankAssemblyAccession(parts[1]);
                return pk;
            }
        };
    }

}
