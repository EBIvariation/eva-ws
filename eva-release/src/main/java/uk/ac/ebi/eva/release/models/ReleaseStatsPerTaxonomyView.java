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

import javax.persistence.*;
import java.util.Objects;

@Entity
@IdClass(ReleaseStatsPerTaxonomyViewPK.class)
@Table(name = "release_rs_count_per_taxonomy_id")
public class ReleaseStatsPerTaxonomyView {

    @Id
    private int taxonomyId;

    @Id
    private int releaseVersion;

    private String[] assemblyAccessions;

    private String rsType;

    private Long count;

    @Column(name="new")
    private Long newAddition;

    public ReleaseStatsPerTaxonomyView() {
    }

    public int getTaxonomyId() {
        return taxonomyId;
    }

    public void setTaxonomyId(int taxonomyId) {
        this.taxonomyId = taxonomyId;
    }

    public String[] getAssemblyAccessions() {
        return assemblyAccessions;
    }

    public void setAssemblyAccessions(String[] assemblyAccessions) {
        this.assemblyAccessions = assemblyAccessions;
    }

    public int getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(int releaseVersion) {
        this.releaseVersion = releaseVersion;
    }


    public String getRsType() {
        return rsType;
    }

    public void setRsType(String rsType) {
        this.rsType = rsType;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReleaseStatsPerTaxonomyView taxonomy = (ReleaseStatsPerTaxonomyView) o;
        return releaseVersion == taxonomy.releaseVersion &&
                Objects.equals(taxonomyId, taxonomy.taxonomyId) &&
                Objects.equals(rsType, taxonomy.rsType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taxonomyId, releaseVersion, rsType);
    }

    @Override
    public String toString() {
        return "ReleaseStatsPerAssembly{" +
                ", taxonomyId=" + taxonomyId +
                ", releaseVersion=" + releaseVersion +
                ", rsType='" + rsType + '\'' +
                ", count=" + count +
                ", new=" + newAddition +
                '}';
    }
}
