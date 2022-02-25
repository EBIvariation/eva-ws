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
package uk.ac.ebi.eva.release.mappers;

import org.springframework.stereotype.Component;

import uk.ac.ebi.eva.release.dto.ReleaseStatsPerSpeciesDto;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerSpecies;
import uk.ac.ebi.eva.release.repositories.ReleaseInfoRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReleaseStatsPerSpeciesMapper {

    private static final String SPECIES_DIRECTORY = "by_species/";

    private static final String TAXONOMY_URL = "https://www.ebi.ac.uk/ena/browser/view/Taxon:";

    private final ReleaseInfoRepository releaseInfoRepository;

    public ReleaseStatsPerSpeciesMapper(ReleaseInfoRepository releaseInfoRepository) {
        this.releaseInfoRepository = releaseInfoRepository;
    }

    public Iterable<ReleaseStatsPerSpeciesDto> toDto(Iterable<ReleaseStatsPerSpecies> releaseStatsPerSpecies) {
        List<ReleaseStatsPerSpeciesDto> releaseStatsPerSpeciesDtos = new ArrayList();
        for (ReleaseStatsPerSpecies species : releaseStatsPerSpecies) {
            releaseStatsPerSpeciesDtos.add(toDto(species));
        }
        return releaseStatsPerSpeciesDtos;
    }

    private ReleaseStatsPerSpeciesDto toDto(ReleaseStatsPerSpecies releaseStatsPerSpecies) {
        Map<Integer, String> releasesFtp = getReleasesFtp();

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
        String releaseLink = releasesFtp.get(releaseStatsPerSpecies.getReleaseVersion()) + SPECIES_DIRECTORY +
                releaseStatsPerSpecies.getReleaseFolder();
        releaseStatsPerSpeciesDto.setReleaseLink(releaseLink);
        releaseStatsPerSpeciesDto.setTaxonomyLink(TAXONOMY_URL + releaseStatsPerSpecies.getTaxonomyId());
        return releaseStatsPerSpeciesDto;
    }

    private Map<Integer, String> getReleasesFtp() {
        Map<Integer, String> releaseFtp = new HashMap<>();
        releaseInfoRepository.findAll().forEach(r -> releaseFtp.put(r.getReleaseVersion(), r.getReleaseFtp()));
        return releaseFtp;
    }
}
