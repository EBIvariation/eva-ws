/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.release.dto;

import javax.persistence.Id;
import java.util.Objects;

public class ReleaseStatsPerSpeciesV2Dto extends ReleaseStatsPerV2Dto {

    @Id
    private int taxonomyId;

    private String taxonomyLink;

    private String scientificName;

    public ReleaseStatsPerSpeciesV2Dto() {
    }

    public int getTaxonomyId() {
        return taxonomyId;
    }

    public void setTaxonomyId(int taxonomyId) {
        this.taxonomyId = taxonomyId;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getTaxonomyLink() {
        return taxonomyLink;
    }

    public void setTaxonomyLink(String taxonomyLink) {
        this.taxonomyLink = taxonomyLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReleaseStatsPerSpeciesV2Dto that = (ReleaseStatsPerSpeciesV2Dto) o;
        return taxonomyId == that.taxonomyId &&
                releaseVersion == that.releaseVersion &&
                Objects.equals(scientificName, that.scientificName) &&
                Objects.equals(releaseFolder, that.releaseFolder) &&
                Objects.equals(currentRs, that.currentRs) &&
                Objects.equals(multiMappedRs, that.multiMappedRs) &&
                Objects.equals(mergedRs, that.mergedRs) &&
                Objects.equals(deprecatedRs, that.deprecatedRs) &&
                Objects.equals(mergedDeprecatedRs, that.mergedDeprecatedRs) &&
                Objects.equals(unmappedRs, that.unmappedRs) &&
                Objects.equals(newCurrentRs, that.newCurrentRs) &&
                Objects.equals(newMultiMappedRs, that.newMultiMappedRs) &&
                Objects.equals(newMergedRs, that.newMergedRs) &&
                Objects.equals(newDeprecatedRs, that.newDeprecatedRs) &&
                Objects.equals(newMergedDeprecatedRs, that.newMergedDeprecatedRs) &&
                Objects.equals(newUnmappedRs, that.newUnmappedRs) &&
                Objects.equals(releaseLink, that.releaseLink) &&
                Objects.equals(taxonomyLink, that.taxonomyLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taxonomyId, releaseVersion, scientificName, releaseFolder, currentRs, multiMappedRs,
                            mergedRs,
                            deprecatedRs, mergedDeprecatedRs, unmappedRs, newCurrentRs, newMultiMappedRs, newMergedRs,
                            newDeprecatedRs, newMergedDeprecatedRs, newUnmappedRs, releaseLink,
                            taxonomyLink);
    }

    @Override
    public String toString() {
        return "ReleaseStatsPerSpeciesDto{" +
                "taxonomyId=" + taxonomyId +
                ", releaseVersion=" + releaseVersion +
                ", scientificName='" + scientificName + '\'' +
                ", releaseFolder='" + releaseFolder + '\'' +
                ", currentRs=" + getCurrentRs() +
                ", multiMappedRs=" + getMultiMappedRs() +
                ", mergedRs=" + getMergedRs() +
                ", deprecatedRs=" + getDeprecatedRs() +
                ", mergedDeprecatedRs=" + getMergedDeprecatedRs() +
                ", unmappedRs=" + getUnmappedRs() +
                ", newCurrentRs=" + getNewCurrentRs() +
                ", newMultiMappedRs=" + getNewMultiMappedRs() +
                ", newMergedRs=" + getNewMergedRs() +
                ", newDeprecatedRs=" + getNewDeprecatedRs() +
                ", newMergedDeprecatedRs=" + getNewMergedDeprecatedRs() +
                ", newUnmappedRs=" + getNewUnmappedRs() +
                ", releaseLink='" + releaseLink + '\'' +
                ", taxonomyLink='" + taxonomyLink + '\'' +
                '}';
    }
}
