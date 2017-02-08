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

package uk.ac.ebi.eva.commons.models.data;

import uk.ac.ebi.eva.lib.repository.VariantSourceEntityRepository;

/**
 * Subset of fields from VariantSourceEntity. This is used to make an automatic MongoDB projection in
 * {@link VariantSourceEntityRepository}
 */
public class StudyName implements Comparable {

    private String studyId;

    private String studyName;

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public String getStudyId() {
        return studyId;
    }

    public String getStudyName() {
        return studyName;
    }

    @Override
    public int compareTo(Object o) {
        return studyId.compareTo(((StudyName) o).getStudyId());
    }
}
