/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
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
/*
package uk.ac.ebi.eva.server.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class TestRestConfiguration {

    @Bean
    public RestTemplateBuilder restTemplateBuilder(
            ObjectProvider<HttpMessageConverters> messageConverters,
            ObjectProvider<List<RestTemplateCustomizer>> restTemplateCustomizers) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/hal+json"));
        converter.setObjectMapper(mapper);

        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();

        HttpMessageConverters converters = (HttpMessageConverters) messageConverters.getIfUnique();
        restTemplateBuilder.additionalMessageConverters(converter);
        if (converters != null) {
            restTemplateBuilder.additionalMessageConverters(converters.getConverters());
        }
        List<RestTemplateCustomizer> customizers = (List) restTemplateCustomizers.getIfAvailable();
        if (!CollectionUtils.isEmpty(customizers)) {
            customizers = new ArrayList(customizers);
            AnnotationAwareOrderComparator.sort(customizers);
            restTemplateBuilder = restTemplateBuilder.customizers(customizers);
        }

        return restTemplateBuilder;

    }
}
*/