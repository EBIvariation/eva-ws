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
package uk.ac.ebi.eva.release.dto;

import javax.persistence.Id;
import java.util.Objects;

public class ReleaseStatsPerAssemblyV2Dto extends ReleaseStatsPerV2Dto {

    @Id
    private String assemblyAccession;

    @Id
    private int releaseVersion;

    private int[] taxonomyIds;

    private String releaseLink;

    private String taxonomyLink;

    public ReleaseStatsPerAssemblyV2Dto() {
    }

    public int[] getTaxonomyIds() {
        return taxonomyIds;
    }

    public void setTaxonomyId(int[] taxonomyIds) {
        this.taxonomyIds = taxonomyIds;
    }

    public String getAssemblyAccession() {
        return assemblyAccession;
    }

    public void setAssemblyAccession(String assemblyAccession) {
        this.assemblyAccession = assemblyAccession;
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
        ReleaseStatsPerAssemblyV2Dto that = (ReleaseStatsPerAssemblyV2Dto) o;
        return releaseVersion == that.releaseVersion &&
                Objects.equals(assemblyAccession, that.assemblyAccession);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assemblyAccession, releaseVersion);
    }

    @Override
    public String toString() {
        return "ReleaseStatsPerAssemblyDto{" +
                "assemblyAccession='" + assemblyAccession + '\'' +
                ", releaseVersion=" + releaseVersion +
                ", taxonomyIds=" + taxonomyIds +
                ", releaseFolder='" + releaseFolder + '\'' +
                ", currentRs=" + getCurrentRs() +
                ", multiMappedRs=" + getMultiMappedRs() +
                ", mergedRs=" + getMergedRs() +
                ", deprecatedRs=" + getDeprecatedRs() +
                ", mergedDeprecatedRs=" + getMergedDeprecatedRs() +
                ", newCurrentRs=" + getNewCurrentRs() +
                ", newMultiMappedRs=" + getNewMultiMappedRs() +
                ", newMergedRs=" + getNewMergedRs() +
                ", newDeprecatedRs=" + getNewDeprecatedRs() +
                ", newMergedDeprecatedRs=" + getNewMergedDeprecatedRs() +
                ", releaseLink='" + releaseLink + '\'' +
                ", taxonomyLink='" + taxonomyLink + '\'' +
                '}';
    }
}
