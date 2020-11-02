/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
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
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "release_rs")
public class ReleaseInfo {

    @Id
    private int releaseVersion;

    private LocalDateTime releaseDate;

    private String releaseDescription;

    private String releaseFtp;

    public ReleaseInfo() {
    }

    public ReleaseInfo(int releaseVersion, LocalDateTime releaseDate, String releaseDescription, String releaseFtp){
        this.releaseVersion = releaseVersion;
        this.releaseDate = releaseDate;
        this.releaseDescription = releaseDescription;
        this.releaseFtp = releaseFtp;
    }

    public int getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(int releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getReleaseDescription() {
        return releaseDescription;
    }

    public void setReleaseDescription(String releaseDescription) {
        this.releaseDescription = releaseDescription;
    }

    public String getReleaseFtp() {
        return releaseFtp;
    }

    public void setReleaseFtp(String releaseFtp) {
        this.releaseFtp = releaseFtp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReleaseInfo that = (ReleaseInfo) o;
        return releaseVersion == that.releaseVersion &&
                Objects.equals(releaseDate, that.releaseDate) &&
                Objects.equals(releaseDescription, that.releaseDescription) &&
                Objects.equals(releaseFtp, that.releaseFtp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(releaseVersion, releaseDate, releaseDescription, releaseFtp);
    }

    @Override
    public String toString() {
        return "ReleaseInfo{" +
                "releaseVersion=" + releaseVersion +
                ", releaseDate=" + releaseDate +
                ", releaseDescription='" + releaseDescription + '\'' +
                ", releaseFtp='" + releaseFtp + '\'' +
                '}';
    }
}
