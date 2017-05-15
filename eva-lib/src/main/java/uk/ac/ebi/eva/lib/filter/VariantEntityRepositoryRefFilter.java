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

package uk.ac.ebi.eva.lib.filter;

import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

import java.util.List;

public class VariantEntityRepositoryRefFilter extends VariantEntityRepositoryFilter<String> {

    private static final String FIELD = VariantEntityRepositoryFilter.REF_FIELD;

    public VariantEntityRepositoryRefFilter(String ref) {
        super(FIELD, ref, VariantEntityRepository.RelationalOperator.EQ);
    }
}
