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

import uk.ac.ebi.eva.release.dto.ReleaseStatsPerAssemblyDto;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerAssembly;
import uk.ac.ebi.eva.release.repositories.ReleaseInfoRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReleaseStatsPerAssemblyMapper {

    private static final String SPECIES_DIRECTORY = "by_species/";

    private static final String TAXONOMY_URL = "https://www.ebi.ac.uk/ena/browser/view/Taxon:";

    private final ReleaseInfoRepository releaseInfoRepository;

    public ReleaseStatsPerAssemblyMapper(ReleaseInfoRepository releaseInfoRepository) {
        this.releaseInfoRepository = releaseInfoRepository;
    }

    public Iterable<ReleaseStatsPerAssemblyDto> toDto(Iterable<ReleaseStatsPerAssembly> releaseStatsPerAssembly) {
        List<ReleaseStatsPerAssemblyDto> releaseStatsPerAssemblyDtos = new ArrayList();
        for (ReleaseStatsPerAssembly assembly : releaseStatsPerAssembly) {
            releaseStatsPerAssemblyDtos.add(toDto(assembly));
        }
        return releaseStatsPerAssemblyDtos;
    }

    private ReleaseStatsPerAssemblyDto toDto(ReleaseStatsPerAssembly releaseStatsPerAssembly) {
        Map<Integer, String> releasesFtp = getReleasesFtp();

        ReleaseStatsPerAssemblyDto releaseStatsPerAssemblyDto = new ReleaseStatsPerAssemblyDto();
        releaseStatsPerAssemblyDto.setTaxonomyId(releaseStatsPerAssembly.getTaxonomyId());
        releaseStatsPerAssemblyDto.setAssemblyAccession(releaseStatsPerAssembly.getAssemblyAccession());
        releaseStatsPerAssemblyDto.setReleaseVersion(releaseStatsPerAssembly.getReleaseVersion());
        releaseStatsPerAssemblyDto.setScientificName(releaseStatsPerAssembly.getScientificName());
        releaseStatsPerAssemblyDto.setReleaseFolder(releaseStatsPerAssembly.getReleaseFolder());
        releaseStatsPerAssemblyDto.setCurrentRs(releaseStatsPerAssembly.getCurrentRs());
        releaseStatsPerAssemblyDto.setNewRemappedCurrentRs(releaseStatsPerAssembly.getRemappedCurrentRs());
        releaseStatsPerAssemblyDto.setMultiMappedRs(releaseStatsPerAssembly.getMultiMappedRs());
        releaseStatsPerAssemblyDto.setMergedRs(releaseStatsPerAssembly.getMergedRs());
        releaseStatsPerAssemblyDto.setSplitRs(releaseStatsPerAssembly.getSplitRs());
        releaseStatsPerAssemblyDto.setDeprecatedRs(releaseStatsPerAssembly.getDeprecatedRs());
        releaseStatsPerAssemblyDto.setMergedDeprecatedRs(releaseStatsPerAssembly.getMergedDeprecatedRs());
        releaseStatsPerAssemblyDto.setSsClustered(releaseStatsPerAssembly.getSsClustered());
        releaseStatsPerAssemblyDto.setNewCurrentRs(releaseStatsPerAssembly.getNewCurrentRs());
        releaseStatsPerAssemblyDto.setNewRemappedCurrentRs(releaseStatsPerAssembly.getNewRemappedCurrentRs());
        releaseStatsPerAssemblyDto.setNewMultiMappedRs(releaseStatsPerAssembly.getNewMultiMappedRs());
        releaseStatsPerAssemblyDto.setNewMergedRs(releaseStatsPerAssembly.getNewMergedRs());
        releaseStatsPerAssemblyDto.setNewSplitRs(releaseStatsPerAssembly.getNewSplitRs());
        releaseStatsPerAssemblyDto.setNewDeprecatedRs(releaseStatsPerAssembly.getNewDeprecatedRs());
        releaseStatsPerAssemblyDto.setNewMergedDeprecatedRs(releaseStatsPerAssembly.getNewMergedDeprecatedRs());
        releaseStatsPerAssemblyDto.setNewSsClustered(releaseStatsPerAssembly.getNewSsClustered());
        String releaseLink = releasesFtp.get(releaseStatsPerAssembly.getReleaseVersion()) + SPECIES_DIRECTORY +
                releaseStatsPerAssembly.getReleaseFolder();
        releaseStatsPerAssemblyDto.setReleaseLink(releaseLink);
        releaseStatsPerAssemblyDto.setTaxonomyLink(TAXONOMY_URL + releaseStatsPerAssembly.getTaxonomyId());
        return releaseStatsPerAssemblyDto;
    }

    private Map<Integer, String> getReleasesFtp() {
        Map<Integer, String> releaseFtp = new HashMap<>();
        releaseInfoRepository.findAll().forEach(r -> releaseFtp.put(r.getReleaseVersion(), r.getReleaseFtp()));
        return releaseFtp;
    }

}
