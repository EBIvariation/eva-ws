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
package uk.ac.ebi.eva.server.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.ac.ebi.eva.commons.core.models.ConsequenceType;
import uk.ac.ebi.eva.commons.core.models.VariantStatistics;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.lib.json.ConsequenceTypeMixin;
import uk.ac.ebi.eva.lib.json.QueryResponseMixin;
import uk.ac.ebi.eva.lib.json.VariantMixin;
import uk.ac.ebi.eva.lib.json.VariantStatisticsMixin;
import uk.ac.ebi.eva.lib.json.VariantStudyMixin;
import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.utils.QueryResponse;

@Configuration
public class JacksonConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.addMixIn(VariantWithSamplesAndAnnotation.class, VariantMixin.class);
        objectMapper.addMixIn(QueryResponse.class, QueryResponseMixin.class);
        objectMapper.addMixIn(VariantStudy.class, VariantStudyMixin.class);
        objectMapper.addMixIn(VariantStatistics.class, VariantStatisticsMixin.class);
        objectMapper.addMixIn(ConsequenceType.class, ConsequenceTypeMixin.class);
        objectMapper.addMixIn(Variant.class,VariantMixin.class);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        VisibilityChecker<?> vc = objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                                              .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                                              .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                                              .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                                              .withCreatorVisibility(JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(vc);
        return objectMapper;
    }

}
