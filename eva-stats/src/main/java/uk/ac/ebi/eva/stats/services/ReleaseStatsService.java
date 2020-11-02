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
package uk.ac.ebi.eva.stats.services;

import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.stats.models.ReleaseStatsPerSpecies;
import uk.ac.ebi.eva.stats.repositories.ReleaseStatsPerSpeciesRepository;

@Service
public class ReleaseStatsService {

    private final ReleaseStatsPerSpeciesRepository releaseStatsPerSpeciesRepository;

    public ReleaseStatsService(ReleaseStatsPerSpeciesRepository releaseStatsPerSpeciesRepository) {
        this.releaseStatsPerSpeciesRepository = releaseStatsPerSpeciesRepository;
    }

    public Iterable<ReleaseStatsPerSpecies> getReleaseStatsPerSpecies(Integer releaseVersion,
                                                                      boolean excludeUnmappedOnly) {
        Iterable<ReleaseStatsPerSpecies> releaseData;
        if (releaseVersion != null) {
            if (excludeUnmappedOnly) {
                releaseData = getReleaseDataByVersionExcludingUnmappedOnly(releaseVersion);
            } else {
                releaseData = releaseStatsPerSpeciesRepository.findAllByReleaseVersion(releaseVersion);
            }
        } else {
            if (excludeUnmappedOnly) {
                releaseData = getReleaseDataExcludingUnmappedOnly();
            } else {
                releaseData = releaseStatsPerSpeciesRepository.findAll();
            }
        }
        return releaseData;
    }

    private Iterable<ReleaseStatsPerSpecies> getReleaseDataByVersionExcludingUnmappedOnly(Integer releaseVersion) {
        return releaseStatsPerSpeciesRepository
                .findByReleaseVersionAndCurrentRsNotAndMultiMappedRsNotAndMergedRsNotAndDeprecatedRsNotAndMergedDeprecatedRsNotAndUnmappedRsGreaterThan(
                        releaseVersion, 0, 0, 0, 0, 0, 0);
    }

    private Iterable<ReleaseStatsPerSpecies> getReleaseDataExcludingUnmappedOnly() {
        return releaseStatsPerSpeciesRepository
                .findByCurrentRsNotAndMultiMappedRsNotAndMergedRsNotAndDeprecatedRsNotAndMergedDeprecatedRsNotAndUnmappedRsGreaterThan(
                        0, 0, 0, 0, 0, 0);
    }

}
