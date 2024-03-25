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

public abstract class ReleaseStatsV2Dto {

    protected int releaseVersion;

    protected String releaseFolder;

    protected Long currentRs;

    protected Long mergedRs;

    protected Long deprecatedRs;

    protected Long mergedDeprecatedRs;

    protected Long newCurrentRs;

    protected Long newMergedRs;

    protected Long newDeprecatedRs;

    protected Long newMergedDeprecatedRs;

    protected Long multiMappedRs;

    protected Long newMultiMappedRs;

    protected Long unmappedRs;

    protected Long newUnmappedRs;

    protected String releaseLink;

    public ReleaseStatsV2Dto() {
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

        if (currentRs == null) {
            return new Long(0);
        }
        return currentRs;
    }

    public void setCurrentRs(Long currentRs) {
        this.currentRs = currentRs;
    }

    public Long getMergedRs() {
        if (mergedRs == null) {
            return new Long(0);
        }
        return mergedRs;
    }

    public void setMergedRs(Long mergedRs) {
        this.mergedRs = mergedRs;
    }

    public Long getDeprecatedRs() {
        if (deprecatedRs == null) {
            return new Long(0);
        }
        return deprecatedRs;
    }

    public void setDeprecatedRs(Long deprecatedRs) {
        this.deprecatedRs = deprecatedRs;
    }

    public Long getMergedDeprecatedRs() {
        if (mergedDeprecatedRs == null) {
            return new Long(0);
        }
        return mergedDeprecatedRs;
    }

    public void setMergedDeprecatedRs(Long mergedDeprecatedRs) {
        this.mergedDeprecatedRs = mergedDeprecatedRs;
    }

    public Long getNewCurrentRs() {
        if (newCurrentRs == null) {
            return new Long(0);
        }
        return newCurrentRs;
    }

    public void setNewCurrentRs(Long newCurrentRs) {
        this.newCurrentRs = newCurrentRs;
    }

    public Long getNewMergedRs() {
        if (newMergedRs == null) {
            return new Long(0);
        }
        return newMergedRs;
    }

    public void setNewMergedRs(Long newMergedRs) {
        this.newMergedRs = newMergedRs;
    }

    public Long getNewDeprecatedRs() {
        if (newDeprecatedRs == null) {
            return new Long(0);
        }
        return newDeprecatedRs;
    }

    public void setNewDeprecatedRs(Long newDeprecatedRs) {
        this.newDeprecatedRs = newDeprecatedRs;
    }

    public Long getNewMergedDeprecatedRs() {
        if (newMergedDeprecatedRs == null) {
            return new Long(0);
        }
        return newMergedDeprecatedRs;
    }

    public void setNewMergedDeprecatedRs(Long newMergedDeprecatedRs) {
        this.newMergedDeprecatedRs = newMergedDeprecatedRs;
    }

    public Long getMultiMappedRs() {
        if (multiMappedRs == null) {
            return new Long(0);
        }
        return multiMappedRs;
    }

    public void setMultiMappedRs(Long multiMappedRs) {
        this.multiMappedRs = multiMappedRs;
    }

    public Long getNewMultiMappedRs() {
        if (newMultiMappedRs == null) {
            return new Long(0);
        }
        return newMultiMappedRs;
    }

    public void setNewMultiMappedRs(Long newMultiMappedRs) {
        this.newMultiMappedRs = newMultiMappedRs;
    }

    public Long getUnmappedRs() {
        if (unmappedRs == null) {
            return new Long(0);
        }
        return unmappedRs;
    }

    public void setUnmappedRs(Long unmappedRs) {
        this.unmappedRs = unmappedRs;
    }

    public Long getNewUnmappedRs() {
        if (newUnmappedRs == null) {
            return new Long(0);
        }
        return newUnmappedRs;
    }

    public void setNewUnmappedRs(Long newUnmappedRs) {
        this.newUnmappedRs = newUnmappedRs;
    }

    public String getReleaseLink() {
        return releaseLink;
    }

    public void setReleaseLink(String releaseLink) {
        this.releaseLink = releaseLink;
    }

}
