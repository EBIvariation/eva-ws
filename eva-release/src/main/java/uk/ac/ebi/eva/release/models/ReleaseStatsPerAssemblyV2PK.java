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

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ReleaseStatsPerAssemblyV2PK implements Serializable {

    private String assemblyAccession;

    private int releaseVersion;

    public ReleaseStatsPerAssemblyV2PK() {
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
        ReleaseStatsPerAssemblyV2PK assembly = (ReleaseStatsPerAssemblyV2PK) o;
        return releaseVersion == assembly.releaseVersion &&
                Objects.equals(assemblyAccession, assembly.assemblyAccession);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assemblyAccession, releaseVersion);
    }

    @Override
    public String toString() {
        return "ReleaseStatsPerAssembly{" +
                ", assemblyAccession='" + assemblyAccession + '\'' +
                ", releaseVersion=" + releaseVersion +
                '}';
    }
}
