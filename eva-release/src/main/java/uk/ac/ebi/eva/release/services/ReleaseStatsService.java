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

import uk.ac.ebi.eva.release.dto.ReleaseStatsPerSpeciesDto;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerSpecies;
import uk.ac.ebi.eva.release.repositories.ReleaseStatsPerSpeciesRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReleaseStatsService {

    private static final String FTP_RELEASE_URL = "ftp://ftp.ebi.ac.uk/pub/databases/eva/rs_releases/release_";

    private static final String SPECIES_DIRECTORY = "/by_species/";

    private static final String TAXONOMY_URL = "https://www.ebi.ac.uk/ena/browser/view/Taxon:";

    private final ReleaseStatsPerSpeciesRepository releaseStatsPerSpeciesRepository;

    public ReleaseStatsService(ReleaseStatsPerSpeciesRepository releaseStatsPerSpeciesRepository) {
        this.releaseStatsPerSpeciesRepository = releaseStatsPerSpeciesRepository;
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
        return toDto(releaseData);
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

    private Iterable<ReleaseStatsPerSpeciesDto> toDto(Iterable<ReleaseStatsPerSpecies> releaseStatsPerSpecies) {
        List<ReleaseStatsPerSpeciesDto> releaseStatsPerSpeciesDtos = new ArrayList();
        for (ReleaseStatsPerSpecies species : releaseStatsPerSpecies) {
            releaseStatsPerSpeciesDtos.add(toDto(species));
        }
        return releaseStatsPerSpeciesDtos;
    }

    private ReleaseStatsPerSpeciesDto toDto(ReleaseStatsPerSpecies releaseStatsPerSpecies) {
        ReleaseStatsPerSpeciesDto releaseStatsPerSpeciesDto = new ReleaseStatsPerSpeciesDto();
        releaseStatsPerSpeciesDto.setTaxonomyId(releaseStatsPerSpecies.getTaxonomyId());
        releaseStatsPerSpeciesDto.setReleaseVersion(releaseStatsPerSpecies.getReleaseVersion());
        releaseStatsPerSpeciesDto.setScientificName(releaseStatsPerSpecies.getScientificName());
        releaseStatsPerSpeciesDto.setReleaseFolder(releaseStatsPerSpecies.getReleaseFolder());
        releaseStatsPerSpeciesDto.setCurrentRs(releaseStatsPerSpecies.getCurrentRs());
        releaseStatsPerSpeciesDto.setMultiMappedRs(releaseStatsPerSpecies.getMultiMappedRs());
        releaseStatsPerSpeciesDto.setMergedRs(releaseStatsPerSpecies.getMergedRs());
        releaseStatsPerSpeciesDto.setDeprecatedRs(releaseStatsPerSpecies.getDeprecatedRs());
        releaseStatsPerSpeciesDto.setMergedDeprecatedRs(releaseStatsPerSpecies.getMergedDeprecatedRs());
        releaseStatsPerSpeciesDto.setUnmappedRs(releaseStatsPerSpecies.getUnmappedRs());
        releaseStatsPerSpeciesDto.setNewCurrentRs(releaseStatsPerSpecies.getNewCurrentRs());
        releaseStatsPerSpeciesDto.setNewMultiMappedRs(releaseStatsPerSpecies.getNewMultiMappedRs());
        releaseStatsPerSpeciesDto.setNewMergedRs(releaseStatsPerSpecies.getNewMergedRs());
        releaseStatsPerSpeciesDto.setNewDeprecatedRs(releaseStatsPerSpecies.getNewDeprecatedRs());
        releaseStatsPerSpeciesDto.setNewMergedDeprecatedRs(releaseStatsPerSpecies.getNewMergedDeprecatedRs());
        releaseStatsPerSpeciesDto.setNewUnmappedRs(releaseStatsPerSpecies.getNewUnmappedRs());
        releaseStatsPerSpeciesDto.setNewSsClustered(releaseStatsPerSpecies.getNewSsClustered());
        releaseStatsPerSpeciesDto.setReleaseLink(FTP_RELEASE_URL + releaseStatsPerSpecies.getReleaseVersion()
                                                + SPECIES_DIRECTORY + releaseStatsPerSpecies.getReleaseFolder());
        releaseStatsPerSpeciesDto.setTaxonomyLink(TAXONOMY_URL + releaseStatsPerSpecies.getTaxonomyId());
        return releaseStatsPerSpeciesDto;
    }
}
