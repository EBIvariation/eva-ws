/*
 * Copyright 2022 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.release.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@IdClass(ReleaseStatsPerAssemblyPK.class)
@Table(name = "release_rs_statistics_per_assembly", schema="eva_stats")
public class ReleaseStatsPerAssembly {

    @Id
    private int taxonomyId;

    @Id
    private String assemblyAccession;

    @Id
    private int releaseVersion;

    private String scientificName;

    private String releaseFolder;

    private Long currentRs;

    private Long remappedCurrentRs;

    private Long multiMappedRs;

    private Long mergedRs;

    private Long splitRs;

    private Long deprecatedRs;

    private Long mergedDeprecatedRs;

    private Long ssClustered;

    private Long newCurrentRs;

    private Long newRemappedCurrentRs;

    private Long newMultiMappedRs;

    private Long newMergedRs;

    private Long newSplitRs;

    private Long newDeprecatedRs;

    private Long newMergedDeprecatedRs;

    private Long newSsClustered;

    private Long clusteredCurrentRs;

    private Long newClusteredCurrentRs;

    public ReleaseStatsPerAssembly() {
    }

    public int getTaxonomyId() {
        return taxonomyId;
    }

    public void setTaxonomyId(int taxonomyId) {
        this.taxonomyId = taxonomyId;
    }

    public String getAssemblyAccession() {
        return assemblyAccession;
    }

    public void setAssemblyAccession(String assemblyAccession) {
        this.assemblyAccession = assemblyAccession;
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

    public Long getRemappedCurrentRs() {
        return remappedCurrentRs;
    }

    public void setRemappedCurrentRs(Long remappedCurrentRs) {
        this.remappedCurrentRs = remappedCurrentRs;
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

    public Long getSplitRs() {
        return splitRs;
    }

    public void setSplitRs(Long splitRs) {
        this.splitRs = splitRs;
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

    public Long getSsClustered() {
        return ssClustered;
    }

    public void setSsClustered(Long ssClustered) {
        this.ssClustered = ssClustered;
    }

    public Long getNewCurrentRs() {
        return newCurrentRs;
    }

    public void setNewCurrentRs(Long newCurrentRs) {
        this.newCurrentRs = newCurrentRs;
    }

    public Long getNewRemappedCurrentRs() {
        return newRemappedCurrentRs;
    }

    public void setNewRemappedCurrentRs(Long newRemappedCurrentRs) {
        this.newRemappedCurrentRs = newRemappedCurrentRs;
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

    public Long getNewSplitRs() {
        return newSplitRs;
    }

    public void setNewSplitRs(Long newSplitRs) {
        this.newSplitRs = newSplitRs;
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

    public Long getNewSsClustered() {
        return newSsClustered;
    }

    public void setNewSsClustered(Long newSsClustered) {
        this.newSsClustered = newSsClustered;
    }

    public Long getClusteredCurrentRs() {
        return clusteredCurrentRs;
    }

    public void setClusteredCurrentRs(Long clusteredCurrentRs) {
        this.clusteredCurrentRs = clusteredCurrentRs;
    }

    public Long getNewClusteredCurrentRs() {
        return newClusteredCurrentRs;
    }

    public void setNewClusteredCurrentRs(Long newClusteredCurrentRs) {
        this.newClusteredCurrentRs = newClusteredCurrentRs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReleaseStatsPerAssembly assembly = (ReleaseStatsPerAssembly) o;
        return taxonomyId == assembly.taxonomyId && releaseVersion == assembly.releaseVersion && Objects.equals(
                assemblyAccession, assembly.assemblyAccession) && Objects.equals(scientificName,
                                                                                 assembly.scientificName) && Objects.equals(
                releaseFolder, assembly.releaseFolder) && Objects.equals(currentRs,
                                                                         assembly.currentRs) && Objects.equals(
                remappedCurrentRs, assembly.remappedCurrentRs) && Objects.equals(multiMappedRs,
                                                                                 assembly.multiMappedRs) && Objects.equals(
                mergedRs, assembly.mergedRs) && Objects.equals(splitRs,
                                                               assembly.splitRs) && Objects.equals(
                deprecatedRs, assembly.deprecatedRs) && Objects.equals(mergedDeprecatedRs,
                                                                       assembly.mergedDeprecatedRs) && Objects.equals(
                ssClustered, assembly.ssClustered) && Objects.equals(newCurrentRs,
                                                                     assembly.newCurrentRs) && Objects.equals(
                newRemappedCurrentRs, assembly.newRemappedCurrentRs) && Objects.equals(newMultiMappedRs,
                                                                                       assembly.newMultiMappedRs) && Objects.equals(
                newMergedRs, assembly.newMergedRs) && Objects.equals(newSplitRs,
                                                                     assembly.newSplitRs) && Objects.equals(
                newDeprecatedRs, assembly.newDeprecatedRs) && Objects.equals(newMergedDeprecatedRs,
                                                                             assembly.newMergedDeprecatedRs) && Objects.equals(
                newSsClustered, assembly.newSsClustered) && Objects.equals(clusteredCurrentRs,
                                                                           assembly.clusteredCurrentRs) && Objects.equals(
                newClusteredCurrentRs, assembly.newClusteredCurrentRs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taxonomyId, assemblyAccession, releaseVersion, scientificName, releaseFolder, currentRs,
                            remappedCurrentRs, multiMappedRs, mergedRs, splitRs, deprecatedRs, mergedDeprecatedRs,
                            ssClustered, newCurrentRs, newRemappedCurrentRs, newMultiMappedRs, newMergedRs, newSplitRs,
                            newDeprecatedRs, newMergedDeprecatedRs, newSsClustered, clusteredCurrentRs,
                            newClusteredCurrentRs);
    }

    @Override
    public String toString() {
        return "ReleaseStatsPerAssembly{" +
                "taxonomyId=" + taxonomyId +
                ", assemblyAccession='" + assemblyAccession + '\'' +
                ", releaseVersion=" + releaseVersion +
                ", scientificName='" + scientificName + '\'' +
                ", releaseFolder='" + releaseFolder + '\'' +
                ", currentRs=" + currentRs +
                ", remappedCurrentRs=" + remappedCurrentRs +
                ", multiMappedRs=" + multiMappedRs +
                ", mergedRs=" + mergedRs +
                ", splitRs=" + splitRs +
                ", deprecatedRs=" + deprecatedRs +
                ", mergedDeprecatedRs=" + mergedDeprecatedRs +
                ", ssClustered=" + ssClustered +
                ", newCurrentRs=" + newCurrentRs +
                ", newRemappedCurrentRs=" + newRemappedCurrentRs +
                ", newMultiMappedRs=" + newMultiMappedRs +
                ", newMergedRs=" + newMergedRs +
                ", newSplitRs=" + newSplitRs +
                ", newDeprecatedRs=" + newDeprecatedRs +
                ", newMergedDeprecatedRs=" + newMergedDeprecatedRs +
                ", newSsClustered=" + newSsClustered +
                ", clusteredCurrentRs=" + clusteredCurrentRs +
                ", newClusteredCurrentRs=" + newClusteredCurrentRs +
                '}';
    }
}
