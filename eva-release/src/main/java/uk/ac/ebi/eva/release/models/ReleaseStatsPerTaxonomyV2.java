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

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "release_rs_count_per_taxonomy", schema="eva_stats")
public class ReleaseStatsPerTaxonomyV2 {

    @EmbeddedId
    ReleaseStatsPerTaxonomyV2PK releaseStatsPerTaxonomyV2Id;

    @Column(insertable = false, updatable = false)
    private int releaseVersion;

    @ManyToOne
    @JoinColumn(name="taxonomyId", insertable = false, updatable = false)
    @NotFound(action=NotFoundAction.IGNORE)
    private Taxonomy taxonomy;

    @Type(type = "string-array")
    @Column(
            name = "assembly_accessions",
            columnDefinition = "text[]"
    )
    private String[] assemblyAccessions;

    private String releaseFolder;

    private Long currentRs;

    private Long multimapRs;

    private Long mergedRs;

    private Long deprecatedRs;

    private Long mergedDeprecatedRs;

    private Long unmappedRs;

    private Long newCurrentRs;

    private Long newMultimapRs;

    private Long newMergedRs;

    private Long newDeprecatedRs;

    private Long newMergedDeprecatedRs;

    private Long newUnmappedRs;


    public ReleaseStatsPerTaxonomyV2() {
    }

    public int getTaxonomyId() {
        return releaseStatsPerTaxonomyV2Id.getTaxonomyId();
    }

    public String[] getAssemblyAccessions() {
        return assemblyAccessions;
    }

    public void setAssemblyAccessions(String[] assemblyAccessions) {
        this.assemblyAccessions = assemblyAccessions;
    }

    public int getReleaseVersion() {
        return releaseStatsPerTaxonomyV2Id.getReleaseVersion();
    }

    public void setReleaseVersion(int releaseVersion) {
        this.releaseStatsPerTaxonomyV2Id.setReleaseVersion(releaseVersion);
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

    public Long getNewMultimapRs() {
        return newMultimapRs;
    }

    public void setNewMultimapRs(Long newMultimapRs) {
        this.newMultimapRs = newMultimapRs;
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

    public String getScientificName() {
        if ( this.taxonomy != null) {
            return taxonomy.getScientificName();
        }else {
            return null;
        }
    }

    public String getCommonName() {
        if ( this.taxonomy != null) {
            return taxonomy.getCommonName();
        }else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReleaseStatsPerTaxonomyV2 other_taxonomy = (ReleaseStatsPerTaxonomyV2) o;
        return this.getReleaseVersion() == other_taxonomy.getReleaseVersion() &&
                Objects.equals(this.getTaxonomyId(), other_taxonomy.getTaxonomyId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getTaxonomyId(), this.getReleaseVersion());
    }

}
