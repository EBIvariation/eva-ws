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

package uk.ac.ebi.eva.lib.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.ebi.eva.commons.core.models.VariantType;

@JsonIgnoreProperties({"id"})
public abstract class VariantMixin {
    @JsonInclude(JsonInclude.Include.NON_NULL) String reference;
    @JsonInclude(JsonInclude.Include.NON_NULL) String alternate;
    @JsonProperty("type") abstract VariantType getType();
    @JsonProperty("length") abstract int getLength();
}
