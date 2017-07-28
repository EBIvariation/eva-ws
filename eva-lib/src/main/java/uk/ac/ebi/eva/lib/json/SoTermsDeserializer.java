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

package uk.ac.ebi.eva.lib.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SoTermsDeserializer extends StdDeserializer<Set<Integer>> {

    protected SoTermsDeserializer() {
        super(Set.class);
    }

    @Override
    public Set<Integer> deserialize(JsonParser parser, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = parser.getCodec().readTree(parser);
        Set<Integer> soAccessions = new HashSet<>();
        for (JsonNode element : node) {
            String accession = element.get("soAccession").asText();
            String[] code = accession.split(":");
            Assert.isTrue(code.length == 2);
            soAccessions.add(Integer.parseInt(code[1]));
        }
        return soAccessions;
    }
}
