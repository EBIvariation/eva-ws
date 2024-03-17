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

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Objects;

@Entity
@IdClass(ReleaseStatsPerAssemblyViewPK.class)
@Table(name = "release_rs_count_per_assembly")
public class ReleaseStatsPerAssemblyView implements ReleaseStatsView {

    @Id
    private int releaseVersion;
    @Id
    private String assemblyAccession;

    private String rsType;

//    private int[] taxonomyIds;

    private Long count;

    @Column(name="new")
    protected Long newAddition;
    public ReleaseStatsPerAssemblyView() {
    }

//    public int[] getTaxonomyIds() {
//        return taxonomyIds;
//    }
//
//    public void setTaxonomyIds(int[] taxonomyIds) {
//        this.taxonomyIds = taxonomyIds;
//    }

    public String getAssemblyAccession() {
        return assemblyAccession;
    }

    public void setAssemblyAccession(String assemblyAccession) {
        this.assemblyAccession = assemblyAccession;
    }

    public String getRsType() {
        return rsType;
    }


    public int getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(int releaseVersion) {
        this.releaseVersion = releaseVersion;
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
    public String getKey() {
        return this.assemblyAccession + "_" + this.releaseVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReleaseStatsPerAssemblyView assembly = (ReleaseStatsPerAssemblyView) o;
        return releaseVersion == assembly.releaseVersion &&
                Objects.equals(assemblyAccession, assembly.assemblyAccession) &&
                Objects.equals(rsType, assembly.rsType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assemblyAccession, releaseVersion, rsType);
    }

    @Override
    public String toString() {
        return "ReleaseStatsPerAssembly{" +
                ", assemblyAccession='" + assemblyAccession + '\'' +
                ", releaseVersion=" + releaseVersion +
                ", rsType='" + rsType + '\'' +
                ", count=" + count +
                ", new=" + newAddition +
                '}';
    }
}
