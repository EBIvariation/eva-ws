/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.lib.filter;

import org.junit.Test;

import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

import static org.junit.Assert.*;

public class VariantEntityRepositoryDoubleFilterTest {

    @Test
    public void getValueFromRelation() throws Exception {
        assertEquals(new Double(0.5), VariantEntityRepositoryDoubleFilter.getValueFromRelation("=0.5"));
        assertEquals(new Double(0.12), VariantEntityRepositoryDoubleFilter.getValueFromRelation(">0.12"));
        assertEquals(new Double(0.134), VariantEntityRepositoryDoubleFilter.getValueFromRelation(">=0.134"));
        assertEquals(new Double(1.1), VariantEntityRepositoryDoubleFilter.getValueFromRelation("<1.1"));
        assertEquals(new Double(0.5), VariantEntityRepositoryDoubleFilter.getValueFromRelation("<=0.5"));
    }

    @Test
    public void getRelationalOperatorFromRelation() throws Exception {
        assertEquals(VariantEntityRepository.RelationalOperator.EQ,
                     VariantEntityRepositoryDoubleFilter.getRelationalOperatorFromRelation("=0.5"));
        assertEquals(VariantEntityRepository.RelationalOperator.GT,
                     VariantEntityRepositoryDoubleFilter.getRelationalOperatorFromRelation(">0.12"));
        assertEquals(VariantEntityRepository.RelationalOperator.GTE,
                     VariantEntityRepositoryDoubleFilter.getRelationalOperatorFromRelation(">=0.134"));
        assertEquals(VariantEntityRepository.RelationalOperator.LT,
                     VariantEntityRepositoryDoubleFilter.getRelationalOperatorFromRelation("<1.1"));
        assertEquals(VariantEntityRepository.RelationalOperator.LTE,
                     VariantEntityRepositoryDoubleFilter.getRelationalOperatorFromRelation("<=0.5"));
    }

}
