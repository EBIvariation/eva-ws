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
public class ReleaseStatsPerTaxonomyV2 implements ReleaseStatsV2 {

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

    private Long count;

    @Column(name="new")
    protected Long newAddition;


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

    public String getRsType() {
        return releaseStatsPerTaxonomyV2Id.getRsType();
    }

    public int getReleaseVersion() {
        return releaseStatsPerTaxonomyV2Id.getReleaseVersion();
    }

    public void setReleaseVersion(int releaseVersion) {
        this.releaseStatsPerTaxonomyV2Id.setReleaseVersion(releaseVersion);
    }

    public void setRsType(String rsType) {
        releaseStatsPerTaxonomyV2Id.setRsType(rsType);
    }

    public String getReleaseFolder() {
        return releaseFolder;
    }

    public void setReleaseFolder(String releaseFolder) {
        this.releaseFolder = releaseFolder;
    }


    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getNewAddition() {
        return newAddition;
    }

    public void setNewAddition(Long newAddition) {
        this.newAddition = newAddition;
    }

    @Override
    public String getKey() {
        return this.getTaxonomyId() + "_" + this.getReleaseVersion();
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
                Objects.equals(this.getTaxonomyId(), other_taxonomy.getTaxonomyId()) &&
                Objects.equals(this.getRsType(), other_taxonomy.getRsType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getTaxonomyId(), this.getReleaseVersion(), this.getRsType());
    }

    @Override
    public String toString() {
        return "ReleaseStatsPerAssembly{" +
                ", taxonomyId=" + this.getTaxonomyId() +
                ", releaseVersion=" + this.getReleaseVersion() +
                ", rsType='" + this.getRsType() + '\'' +
                ", count=" + count +
                ", new=" + newAddition +
                '}';
    }
}
