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
import uk.ac.ebi.eva.release.dto.*;
import uk.ac.ebi.eva.release.mappers.ReleaseStatsPerAssemblyMapper;
import uk.ac.ebi.eva.release.mappers.ReleaseStatsPerSpeciesMapper;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerAssembly;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerAssemblyV2;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerSpecies;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerTaxonomyV2;
import uk.ac.ebi.eva.release.repositories.ReleaseStatsPerAssemblyV2Repository;
import uk.ac.ebi.eva.release.repositories.ReleaseStatsPerTaxonomyV2Repository;


@Service
public class ReleaseStatsServiceV2 {

    private final ReleaseStatsPerTaxonomyV2Repository releaseStatsPerTaxonomyRepository;

    private final ReleaseStatsPerAssemblyV2Repository releaseStatsPerAssemblyRepository;

    private final ReleaseStatsPerSpeciesMapper releaseStatsPerSpeciesMapper;

    private final ReleaseStatsPerAssemblyMapper releaseStatsPerAssemblyMapper;

    public ReleaseStatsServiceV2(ReleaseStatsPerTaxonomyV2Repository releaseStatsPerTaxonomyRepository,
                                 ReleaseStatsPerAssemblyV2Repository releaseStatsPerAssemblyRepository,
                                 ReleaseStatsPerSpeciesMapper releaseStatsPerSpeciesMapper,
                                 ReleaseStatsPerAssemblyMapper releaseStatsPerAssemblyMapper) {
        this.releaseStatsPerTaxonomyRepository = releaseStatsPerTaxonomyRepository;
        this.releaseStatsPerAssemblyRepository = releaseStatsPerAssemblyRepository;
        this.releaseStatsPerSpeciesMapper = releaseStatsPerSpeciesMapper;
        this.releaseStatsPerAssemblyMapper = releaseStatsPerAssemblyMapper;
    }


    public Iterable<ReleaseStatsPerSpeciesV2Dto> getReleaseStatsPerSpeciesWithNewRsIds(Integer releaseVersion){
        Iterable<ReleaseStatsPerTaxonomyV2> releaseData;
        releaseData = releaseStatsPerTaxonomyRepository.findAllByReleaseVersion(releaseVersion);
        return this.releaseStatsPerSpeciesMapper.toDtoV2(releaseData);

    }

    public Iterable<ReleaseStatsPerSpeciesV2Dto> getReleaseStatsPerSpecies(Integer releaseVersion,
                                                                           boolean excludeUnmappedOnly) {
        Iterable<ReleaseStatsPerTaxonomyV2> releaseData;
        if (releaseVersion != null) {
            if (excludeUnmappedOnly) {
                releaseData = getReleaseDataByVersionExcludingUnmappedOnly(releaseVersion);
            } else {
                releaseData = releaseStatsPerTaxonomyRepository.findAllByReleaseVersion(releaseVersion);
            }
        } else {
            if (excludeUnmappedOnly) {
                releaseData = getReleaseDataPerSpeciesExcludingUnmappedOnly();
            } else {
                releaseData = releaseStatsPerTaxonomyRepository.findAll();
            }
        }
        return releaseStatsPerSpeciesMapper.toDtoV2(releaseData);
    }

    private Iterable<ReleaseStatsPerTaxonomyV2> getReleaseDataByVersionExcludingUnmappedOnly(Integer releaseVersion) {
        return releaseStatsPerTaxonomyRepository
                .findByReleaseVersionAndCurrentRsNotAndMultimapRsNotAndMergedRsNotAndDeprecatedRsNotAndMergedDeprecatedRsNotAndUnmappedRsGreaterThan(
                        releaseVersion, 0, 0, 0, 0, 0, 0);
    }

    private Iterable<ReleaseStatsPerTaxonomyV2> getReleaseDataPerSpeciesExcludingUnmappedOnly() {
        return releaseStatsPerTaxonomyRepository
                .findByCurrentRsNotAndMultimapRsNotAndMergedRsNotAndDeprecatedRsNotAndMergedDeprecatedRsNotAndUnmappedRsGreaterThan(
                        0, 0, 0, 0, 0, 0);
    }

    public Iterable<ReleaseStatsPerSpeciesV2Dto> getSpeciesWithNewRsIds(Integer releaseVersion) {
        if (releaseVersion != null) {
            return releaseStatsPerSpeciesMapper.toDtoV2(releaseStatsPerTaxonomyRepository
                    .findByReleaseVersionAndNewCurrentRsGreaterThan(releaseVersion, 0L));
        } else {
            return releaseStatsPerSpeciesMapper.toDtoV2(releaseStatsPerTaxonomyRepository.findByNewCurrentRsGreaterThan(0L));
        }
    }

    public Iterable<ReleaseStatsPerAssemblyV2Dto> getReleaseStatsPerAssembly(Integer releaseVersion) {
        Iterable<ReleaseStatsPerAssemblyV2> releaseData;
        if (releaseVersion != null) {
            releaseData = releaseStatsPerAssemblyRepository.findAllByReleaseVersion(releaseVersion);
        } else {
            releaseData = releaseStatsPerAssemblyRepository.findAll();
        }
        return releaseStatsPerAssemblyMapper.toDtoV2(releaseData);
    }

    public Iterable<ReleaseStatsPerAssemblyV2Dto> getReleaseStatsPerAssemblyWithNewRsIds(Integer releaseVersion) {
        if (releaseVersion != null) {
            return releaseStatsPerAssemblyMapper.toDtoV2(
                    releaseStatsPerAssemblyRepository.findByReleaseVersionAndNewCurrentRsGreaterThan(releaseVersion, 0L));
        } else {
            return releaseStatsPerAssemblyMapper.toDtoV2(
                    releaseStatsPerAssemblyRepository.findByNewCurrentRsGreaterThan(0L));
        }
    }

}
