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

import io.hypersistence.utils.hibernate.type.array.IntArrayType;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Objects;

@Entity
@IdClass(ReleaseStatsPerAssemblyV2PK.class)
@Table(name = "release_rs_count_per_assembly", schema="eva_stats")
public class ReleaseStatsPerAssemblyV2 {

    @Id
    private int releaseVersion;
    @Id
    private String assemblyAccession;

    @Type(type = "int-array")
    @Column(
            name = "taxonomy_ids",
            columnDefinition = "integer[]"
    )
    private int[] taxonomyIds;

    private String releaseFolder;

    private Long currentRs;

    private Long multimapRs;

    private Long mergedRs;

    private Long deprecatedRs;

    private Long mergedDeprecatedRs;

    private Long newCurrentRs;

    private Long newMultimapRs;

    private Long newMergedRs;

    private Long newDeprecatedRs;

    private Long newMergedDeprecatedRs;

    public ReleaseStatsPerAssemblyV2() {
    }

    public int[] getTaxonomyIds() {
        return taxonomyIds;
    }

    public void setTaxonomyIds(int[] taxonomyIds) {
        this.taxonomyIds = taxonomyIds;
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
    
    public Long getNewMergedRs() {
        return newMergedRs;
    }

    public void setNewMergedRs(Long newMergedRs) {
        this.newMergedRs = newMergedRs;
    }

    public Long getMultimapRs() {
        return multimapRs;
    }

    public void setMultimapRs(Long multimapRs) {
        this.multimapRs = multimapRs;
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

    public Long getNewCurrentRs() {
        return newCurrentRs;
    }

    public void setNewCurrentRs(Long newCurrentRs) {
        this.newCurrentRs = newCurrentRs;
    }

    public Long getNewMultimapRs() {
        return newMultimapRs;
    }

    public void setNewMultimapRs(Long newMultimapRs) {
        this.newMultimapRs = newMultimapRs;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReleaseStatsPerAssemblyV2 assembly = (ReleaseStatsPerAssemblyV2) o;
        return releaseVersion == assembly.releaseVersion &&
                Objects.equals(assemblyAccession, assembly.assemblyAccession);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assemblyAccession, releaseVersion);
    }

}
