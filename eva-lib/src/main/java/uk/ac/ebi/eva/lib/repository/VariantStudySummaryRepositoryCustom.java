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
package uk.ac.ebi.eva.lib.repository;

import uk.ac.ebi.eva.lib.repository.projections.VariantStudySummary;

import java.util.List;

/**
 * Interface to declare additional repository methods with a custom implementation,
 * instead of the one that Spring Data would provide by default.
 */
interface VariantStudySummaryRepositoryCustom {
    /**
     * Return a {@link VariantStudySummary} of the first study where the given argument matches the ID or the name
     */
    VariantStudySummary findByStudyNameOrStudyId(String studyNameOrId);

    /**
     * For every study, return its {@link VariantStudySummary}
     */
    List<VariantStudySummary> findBy();
}
