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

import java.io.Serializable;
import java.util.Objects;

public class ReleaseStatsPerAssemblyPK implements Serializable {

    private int taxonomyId;

    private String assemblyAccession;

    private int releaseVersion;

    public ReleaseStatsPerAssemblyPK() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReleaseStatsPerAssemblyPK that = (ReleaseStatsPerAssemblyPK) o;
        return taxonomyId == that.taxonomyId && assemblyAccession == that.assemblyAccession
                && releaseVersion == that.releaseVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taxonomyId, assemblyAccession, releaseVersion);
    }

    @Override
    public String toString() {
        return "ReleaseStatsPerAssemblyPK{" +
                "taxonomyId=" + taxonomyId +
                ", assemblyAccession=" + assemblyAccession +
                ", releaseVersion=" + releaseVersion +
                '}';
    }
}
