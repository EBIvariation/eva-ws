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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ReleaseStatsPerTaxonomyV2PK implements Serializable {

    private int taxonomyId;

    private int releaseVersion;

    public ReleaseStatsPerTaxonomyV2PK() {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReleaseStatsPerTaxonomyV2PK taxonomy = (ReleaseStatsPerTaxonomyV2PK) o;
        return releaseVersion == taxonomy.releaseVersion &&
                Objects.equals(taxonomyId, taxonomy.taxonomyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taxonomyId, releaseVersion);
    }

    @Override
    public String toString() {
        return "ReleaseStatsPerAssembly{" +
                ", taxonomyId=" + taxonomyId +
                ", releaseVersion=" + releaseVersion +
                '}';
    }
}
