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

package uk.ac.ebi.eva.lib.repository.projections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.mapping.Field;

import uk.ac.ebi.eva.commons.models.data.VariantSourceEntity;
import uk.ac.ebi.eva.lib.repository.VariantStudySummaryRepository;

/**
 * Subset of fields from VariantSourceEntity. This is used to make an automatic MongoDB projection in
 * {@link VariantStudySummaryRepository}
 */
public class VariantStudySummary implements Comparable {

    private String studyId;

    private String studyName;

    private int filesCount;

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setFilesCount(int filesCount) {
        this.filesCount = filesCount;
    }

    public int getFilesCount() {
        return filesCount;
    }

    /**
     * This method is required to use a Tree of VariantStudySummary to ignore duplicated summaries.
     */
    @Override
    public int compareTo(Object o) {
        return studyId.compareTo(((VariantStudySummary) o).getStudyId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VariantStudySummary)) {
            return false;
        }

        VariantStudySummary that = (VariantStudySummary) o;

        if (getFilesCount() != that.getFilesCount()) {
            return false;
        }
        if (getStudyId() != null ? !getStudyId().equals(that.getStudyId()) : that.getStudyId() != null) {
            return false;
        }
        return getStudyName() != null ? getStudyName().equals(that.getStudyName()) : that.getStudyName() == null;
    }

    @Override
    public int hashCode() {
        int result = getStudyId() != null ? getStudyId().hashCode() : 0;
        result = 31 * result + (getStudyName() != null ? getStudyName().hashCode() : 0);
        result = 31 * result + getFilesCount();
        return result;
    }
}
