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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import uk.ac.ebi.eva.commons.core.models.ConsequenceType;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class SoTermsSerializerTest {
    @Test
    public void serialize() throws Exception {
        // given
        HashSet<Integer> soAccessions = new HashSet<>(Arrays.asList(1578, 276));
        ConsequenceType consequenceType = new ConsequenceType("", "", "", "", "", 0, 0, 0, "", "", null, null,
                                                              soAccessions, 0);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(ConsequenceType.class, ConsequenceTypeMixin.class);
        StringWriter stringWriter = new StringWriter();
        JsonGenerator generator = objectMapper.getFactory().createGenerator(stringWriter);

        // when
        objectMapper.writeValue(generator, consequenceType);

        //then
        JSONObject jsonObject = new JSONObject(stringWriter.getBuffer().toString());
        JSONArray actual = jsonObject.getJSONArray("soAccessions");

        String expectedString = "[{\"soName\":\"miRNA\",\"soAccession\":\"SO:0000276\"},"
                + "{\"soName\":\"stop_lost\",\"soAccession\":\"SO:0001578\"}]";
        assertEquals(expectedString, actual.toString());
    }

}