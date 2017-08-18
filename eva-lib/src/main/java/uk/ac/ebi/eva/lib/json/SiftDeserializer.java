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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import uk.ac.ebi.eva.commons.core.models.Score;

import java.io.IOException;

public class SiftDeserializer extends StdDeserializer<Score> {

    protected SiftDeserializer() {
        super(Score.class);
    }

    @Override
    public Score deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        Score sift = null;
        for (JsonNode element : node) {
            String source = element.get("source").asText();
            if (!source.toLowerCase().equals("sift")) {
                continue;
            }
            double score = element.get("score").asDouble();
            String description = element.get("description").asText();
            sift = new Score(score, description);
        }
        return sift;
    }
}
