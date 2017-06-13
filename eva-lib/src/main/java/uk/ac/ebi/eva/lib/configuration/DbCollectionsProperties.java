/*
 * Copyright 2016-2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.lib.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Size;

@ConfigurationProperties(ignoreUnknownFields = false, prefix = "db.collection-names")
@Component
@Validated
public class DbCollectionsProperties {

    @Size(min = 1)
    private String files;

    @Size(min = 1)
    private String variants;

    @Size(min = 1)
    private String annotationMetadata;


    public String getFiles() {
        return files;
    }

    public void setFiles(String files) {
        this.files = files;
    }

    public String getVariants() {
        return variants;
    }

    public void setVariants(String variants) {
        this.variants = variants;
    }

    public String getAnnotationMetadata() {
        return annotationMetadata;
    }

    public void setAnnotationMetadata(
            String annotationMetadata) {
        this.annotationMetadata = annotationMetadata;
    }
}
