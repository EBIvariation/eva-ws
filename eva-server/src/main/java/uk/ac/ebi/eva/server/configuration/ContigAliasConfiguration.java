/*
 *
 * Copyright 2024 EMBL - European Bioinformatics Institute
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
 *
 */
package uk.ac.ebi.eva.server.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.eva.server.ws.contigalias.ContigAliasInputParameters;
import uk.ac.ebi.eva.server.ws.contigalias.ContigAliasService;

@Configuration
public class ContigAliasConfiguration {

    @Bean(name = "CONTIG_ALIAS_REST_TEMPLATE")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConfigurationProperties(prefix = "contig-alias")
    public ContigAliasInputParameters contigAliasInputParameters() {
        return new ContigAliasInputParameters();
    }

    @Bean
    public ContigAliasService contigAliasService(@Qualifier("CONTIG_ALIAS_REST_TEMPLATE") RestTemplate restTemplate,
                                                 ContigAliasInputParameters contigAliasInputParameters) {
        return new ContigAliasService(restTemplate, contigAliasInputParameters.getUrl());
    }
}
