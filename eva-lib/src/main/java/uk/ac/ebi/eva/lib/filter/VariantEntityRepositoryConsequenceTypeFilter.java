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

import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

import java.util.List;
import java.util.stream.Collectors;

public class VariantEntityRepositoryConsequenceTypeFilter extends VariantEntityRepositoryFilter<List<Integer>> {

    private static final String FIELD = VariantEntityRepositoryFilter.CONSEQUENCE_TYPE_SO_FIELD;

    public VariantEntityRepositoryConsequenceTypeFilter(List<String> consequenceType) {
        super(FIELD,
              consequenceType.stream()
                             .map(c -> Integer.parseInt(c.replaceAll("[^\\d.]", ""), 10))  // parse integer from string
                             .collect(Collectors.toList()),
              VariantEntityRepository.RelationalOperator.IN);
    }
}
