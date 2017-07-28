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


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import uk.ac.ebi.eva.commons.core.models.ConsequenceType;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

/**
 * Created by jmmut on 2017-07-28.
 *
 * @author Jose Miguel Mut Lopez &lt;jmmut@ebi.ac.uk&gt;
 */
public class SoTermsDeserializerTest {
    @Test
    public void deserialize() throws Exception {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.addMixIn(ConsequenceType.class, ConsequenceTypeMixin.class);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                                               .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                                               .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                                               .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                                               .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        // when
        ConsequenceType ct = objectMapper.readValue(
                "{\"cDnaPosition\":0,\"cdsPosition\":0,\"aaPosition\":0,\"relativePosition\":0," +
                        "\"soAccessions\":[{\"soName\":\"miRNA\",\"soAccession\":\"SO:0000276\"}," +
                        "{\"soName\":\"stop_lost\",\"soAccession\":\"SO:0001578\"}]}", ConsequenceType.class);

        //then
        HashSet<Integer> expectedSoAccessions = new HashSet<>(Arrays.asList(1578, 276));
        assertEquals(expectedSoAccessions, ct.getSoAccessions());
    }
}