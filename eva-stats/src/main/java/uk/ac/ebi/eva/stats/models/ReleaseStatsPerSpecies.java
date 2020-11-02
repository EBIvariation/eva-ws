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
package uk.ac.ebi.eva.stats.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@IdClass(ReleaseStartsPerSpeciesPK.class)
@Table(name = "release_rs_statistics_per_species")
public class ReleaseStatsPerSpecies {

    @Id
    private int taxonomyId;

    @Id
    private int releaseVersion;

    private String scientificName;

    private Long currentRs;

    private Long multiMappedRs;

    private Long mergedRs;

    private Long deprecatedRs;

    private Long mergedDeprecatedRs;

    private Long unmappedRs;

    public ReleaseStatsPerSpecies() {
    }

    public ReleaseStatsPerSpecies(int taxonomyId, int releaseVersion, String scientificName, Long currentRs,
                                  Long multiMappedRs, Long mergedRs, Long deprecatedRs, Long mergedDeprecatedRs,
                                  Long unmappedRs) {
        this.taxonomyId = taxonomyId;
        this.releaseVersion = releaseVersion;
        this.scientificName = scientificName;
        this.currentRs = currentRs;
        this.multiMappedRs = multiMappedRs;
        this.mergedRs = mergedRs;
        this.deprecatedRs = deprecatedRs;
        this.mergedDeprecatedRs = mergedDeprecatedRs;
        this.unmappedRs = unmappedRs;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReleaseStatsPerSpecies that = (ReleaseStatsPerSpecies) o;
        return taxonomyId == that.taxonomyId &&
                releaseVersion == that.releaseVersion &&
                Objects.equals(scientificName, that.scientificName) &&
                Objects.equals(currentRs, that.currentRs) &&
                Objects.equals(multiMappedRs, that.multiMappedRs) &&
                Objects.equals(mergedRs, that.mergedRs) &&
                Objects.equals(deprecatedRs, that.deprecatedRs) &&
                Objects.equals(mergedDeprecatedRs, that.mergedDeprecatedRs) &&
                Objects.equals(unmappedRs, that.unmappedRs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taxonomyId, releaseVersion, scientificName, currentRs, multiMappedRs, mergedRs,
                            deprecatedRs,
                            mergedDeprecatedRs, unmappedRs);
    }

    @Override
    public String toString() {
        return "ReleaseStatsPerSpecies{" +
                "taxonomyId=" + taxonomyId +
                ", releaseVersion=" + releaseVersion +
                ", scientificName='" + scientificName + '\'' +
                ", currentRs=" + currentRs +
                ", multiMappedRs=" + multiMappedRs +
                ", mergedRs=" + mergedRs +
                ", deprecatedRs=" + deprecatedRs +
                ", mergedDeprecatedRs=" + mergedDeprecatedRs +
                ", unmappedRs=" + unmappedRs +
                '}';
    }
}
