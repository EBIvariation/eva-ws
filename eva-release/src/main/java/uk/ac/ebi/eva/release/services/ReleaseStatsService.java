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
package uk.ac.ebi.eva.release.services;

import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.release.dto.ReleaseStatsPerAssemblyDto;
import uk.ac.ebi.eva.release.dto.ReleaseStatsPerSpeciesDto;
import uk.ac.ebi.eva.release.mappers.ReleaseStatsPerAssemblyMapper;
import uk.ac.ebi.eva.release.mappers.ReleaseStatsPerSpeciesMapper;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerAssembly;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerSpecies;
import uk.ac.ebi.eva.release.repositories.ReleaseStatsPerAssemblyRepository;
import uk.ac.ebi.eva.release.repositories.ReleaseStatsPerSpeciesRepository;

@Service
public class ReleaseStatsService {

    private final ReleaseStatsPerSpeciesRepository releaseStatsPerSpeciesRepository;

    private final ReleaseStatsPerAssemblyRepository releaseStatsPerAssemblyRepository;

    private final ReleaseStatsPerSpeciesMapper releaseStatsPerSpeciesMapper;

    private final ReleaseStatsPerAssemblyMapper releaseStatsPerAssemblyMapper;

    public ReleaseStatsService(ReleaseStatsPerSpeciesRepository releaseStatsPerSpeciesRepository,
                               ReleaseStatsPerAssemblyRepository releaseStatsPerAssemblyRepository,
                               ReleaseStatsPerSpeciesMapper releaseStatsPerSpeciesMapper,
                               ReleaseStatsPerAssemblyMapper releaseStatsPerAssemblyMapper) {
        this.releaseStatsPerSpeciesRepository = releaseStatsPerSpeciesRepository;
        this.releaseStatsPerAssemblyRepository = releaseStatsPerAssemblyRepository;
        this.releaseStatsPerSpeciesMapper = releaseStatsPerSpeciesMapper;
        this.releaseStatsPerAssemblyMapper = releaseStatsPerAssemblyMapper;
    }

    public Iterable<ReleaseStatsPerSpeciesDto> getReleaseStatsPerSpecies(Integer releaseVersion,
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
        return releaseStatsPerSpeciesMapper.toDto(releaseData);
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

    public Iterable<ReleaseStatsPerSpeciesDto> getSpeciesWithNewRsIds(Integer releaseVersion) {
        if (releaseVersion != null) {
            return releaseStatsPerSpeciesMapper.toDto(releaseStatsPerSpeciesRepository
                                 .findByReleaseVersionAndNewCurrentRsGreaterThan(releaseVersion, 0L));
        } else {
            return releaseStatsPerSpeciesMapper.toDto(releaseStatsPerSpeciesRepository.findByNewCurrentRsGreaterThan(0L));
        }
    }

    public Iterable<ReleaseStatsPerAssemblyDto> getReleaseStatsPerAssembly(Integer releaseVersion) {
        Iterable<ReleaseStatsPerAssembly> releaseData;
        if (releaseVersion != null) {
            releaseData = releaseStatsPerAssemblyRepository.findAllByReleaseVersion(releaseVersion);
        } else {
            releaseData = releaseStatsPerAssemblyRepository.findAll();
        }
        return releaseStatsPerAssemblyMapper.toDto(releaseData);
    }

    public Iterable<ReleaseStatsPerAssemblyDto> getReleaseStatsPerAssemblyWithNewRsIds(Integer releaseVersion) {
        if (releaseVersion != null) {
            return releaseStatsPerAssemblyMapper.toDto(
                    releaseStatsPerAssemblyRepository.findByReleaseVersionAndNewCurrentRsGreaterThan(releaseVersion, 0L));
        } else {
            return releaseStatsPerAssemblyMapper.toDto(
                    releaseStatsPerAssemblyRepository.findByNewCurrentRsGreaterThan(0L));
        }
    }
}
