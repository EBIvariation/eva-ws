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

public class ReleaseStatsPerSpeciesV2Dto {

    @Id
    private int taxonomyId;

    @Id
    private int releaseVersion;

    private String releaseFolder;

    private Long currentRs;

    private Long multiMappedRs;

    private Long mergedRs;

    private Long deprecatedRs;

    private Long mergedDeprecatedRs;

    private Long unmappedRs;

    private Long newCurrentRs;

    private Long newMultiMappedRs;

    private Long newMergedRs;

    private Long newDeprecatedRs;

    private Long newMergedDeprecatedRs;

    private Long newUnmappedRs;

    private String releaseLink;

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

    public int getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(int releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getReleaseFolder() {
        return releaseFolder;
    }

    public void setReleaseFolder(String releaseFolder) {
        this.releaseFolder = releaseFolder;
    }

    public Long getCurrentRs() {
        return currentRs;
    }

    public void setCurrentRs(Long currentRs) {
        this.currentRs = currentRs;
    }

    public Long getMultiMappedRs() {
        return multiMappedRs;
    }

    public void setMultiMappedRs(Long multiMappedRs) {
        this.multiMappedRs = multiMappedRs;
    }

    public Long getMergedRs() {
        return mergedRs;
    }

    public void setMergedRs(Long mergedRs) {
        this.mergedRs = mergedRs;
    }

    public Long getDeprecatedRs() {
        return deprecatedRs;
    }

    public void setDeprecatedRs(Long deprecatedRs) {
        this.deprecatedRs = deprecatedRs;
    }

    public Long getMergedDeprecatedRs() {
        return mergedDeprecatedRs;
    }

    public void setMergedDeprecatedRs(Long mergedDeprecatedRs) {
        this.mergedDeprecatedRs = mergedDeprecatedRs;
    }

    public Long getUnmappedRs() {
        return unmappedRs;
    }

    public void setUnmappedRs(Long unmappedRs) {
        this.unmappedRs = unmappedRs;
    }

    public Long getNewCurrentRs() {
        return newCurrentRs;
    }

    public void setNewCurrentRs(Long newCurrentRs) {
        this.newCurrentRs = newCurrentRs;
    }

    public Long getNewMultiMappedRs() {
        return newMultiMappedRs;
    }

    public void setNewMultiMappedRs(Long newMultiMappedRs) {
        this.newMultiMappedRs = newMultiMappedRs;
    }

    public Long getNewMergedRs() {
        return newMergedRs;
    }

    public void setNewMergedRs(Long newMergedRs) {
        this.newMergedRs = newMergedRs;
    }

    public Long getNewDeprecatedRs() {
        return newDeprecatedRs;
    }

    public void setNewDeprecatedRs(Long newDeprecatedRs) {
        this.newDeprecatedRs = newDeprecatedRs;
    }

    public Long getNewMergedDeprecatedRs() {
        return newMergedDeprecatedRs;
    }

    public void setNewMergedDeprecatedRs(Long newMergedDeprecatedRs) {
        this.newMergedDeprecatedRs = newMergedDeprecatedRs;
    }

    public Long getNewUnmappedRs() {
        return newUnmappedRs;
    }

    public void setNewUnmappedRs(Long newUnmappedRs) {
        this.newUnmappedRs = newUnmappedRs;
    }

    public String getReleaseLink() {
        return releaseLink;
    }

    public void setReleaseLink(String releaseLink) {
        this.releaseLink = releaseLink;
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
                ", currentRs=" + currentRs +
                ", multiMappedRs=" + multiMappedRs +
                ", mergedRs=" + mergedRs +
                ", deprecatedRs=" + deprecatedRs +
                ", mergedDeprecatedRs=" + mergedDeprecatedRs +
                ", unmappedRs=" + unmappedRs +
                ", newCurrentRs=" + newCurrentRs +
                ", newMultiMappedRs=" + newMultiMappedRs +
                ", newMergedRs=" + newMergedRs +
                ", newDeprecatedRs=" + newDeprecatedRs +
                ", newMergedDeprecatedRs=" + newMergedDeprecatedRs +
                ", newUnmappedRs=" + newUnmappedRs +
                ", releaseLink='" + releaseLink + '\'' +
                ", taxonomyLink='" + taxonomyLink + '\'' +
                '}';
    }
}
