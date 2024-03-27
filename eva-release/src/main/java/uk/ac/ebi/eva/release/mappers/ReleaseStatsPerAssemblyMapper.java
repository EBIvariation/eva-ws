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
import uk.ac.ebi.eva.release.dto.ReleaseStatsPerAssemblyV2Dto;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerAssembly;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerAssemblyV2;
import uk.ac.ebi.eva.release.repositories.ReleaseInfoRepository;

import java.util.*;

@Component
public class ReleaseStatsPerAssemblyMapper {

    private static final String ASSEMBLY_DIRECTORY = "by_assembly/";

    private static final String SPECIES_DIRECTORY = "by_species/";

    private static final String TAXONOMY_URL = "https://www.ebi.ac.uk/ena/browser/view/Taxon:";

    private final Map<Integer, String> releaseLinkMap;

    private final ReleaseStatsMapperUtils releaseStatMapperUtils;

    public ReleaseStatsPerAssemblyMapper(ReleaseInfoRepository releaseInfoRepository) {
        this.releaseStatMapperUtils = new ReleaseStatsMapperUtils(releaseInfoRepository);
        this.releaseLinkMap = this.releaseStatMapperUtils.getReleasesFtp();
    }

    public Iterable<ReleaseStatsPerAssemblyV2Dto> toDtoV2(Iterable<ReleaseStatsPerAssemblyV2> releaseData){
        List<ReleaseStatsPerAssemblyV2Dto> releaseStatsPerAssemblyDtos = new ArrayList();
        for (ReleaseStatsPerAssemblyV2 assemblyData : releaseData) {
            releaseStatsPerAssemblyDtos.add(toDtoV2(assemblyData));
        }
        return releaseStatsPerAssemblyDtos;
    }

    private ReleaseStatsPerAssemblyV2Dto toDtoV2(ReleaseStatsPerAssemblyV2 assemblyData) {
        HashMap<String, ReleaseStatsPerAssemblyV2Dto> keyToDto= new HashMap<>();
        ReleaseStatsPerAssemblyV2Dto dto = new ReleaseStatsPerAssemblyV2Dto();
        dto.setAssemblyAccession(assemblyData.getAssemblyAccession());
        dto.setReleaseVersion(assemblyData.getReleaseVersion());
        dto.setReleaseFolder(assemblyData.getReleaseFolder());
        String releaseLink = this.releaseLinkMap.get(assemblyData.getReleaseVersion()) + ASSEMBLY_DIRECTORY +
                assemblyData.getAssemblyAccession();
        dto.setReleaseLink(releaseLink);
        dto.setTaxonomyIds(assemblyData.getTaxonomyIds());
        String[] taxonomyLinks = Arrays.stream(assemblyData.getTaxonomyIds())
                .mapToObj(String::valueOf).map(t -> TAXONOMY_URL + t).toArray(String[]::new);
        dto.setTaxonomyLinks(taxonomyLinks);

        dto.setCurrentRs(assemblyData.getCurrentRs());
        dto.setMultiMappedRs(assemblyData.getMultimapRs());
        dto.setMergedRs(assemblyData.getMergedRs());
        dto.setDeprecatedRs(assemblyData.getDeprecatedRs());
        dto.setMergedDeprecatedRs(assemblyData.getMergedDeprecatedRs());
        dto.setNewCurrentRs(assemblyData.getNewCurrentRs());
        dto.setNewMultiMappedRs(assemblyData.getNewMultimapRs());
        dto.setNewMergedRs(assemblyData.getNewMergedRs());
        dto.setNewDeprecatedRs(assemblyData.getNewDeprecatedRs());
        dto.setNewMergedDeprecatedRs(assemblyData.getNewMergedDeprecatedRs());
        return dto;
    }

    public Iterable<ReleaseStatsPerAssemblyDto> toDto(Iterable<ReleaseStatsPerAssembly> releaseStatsPerAssembly) {
        List<ReleaseStatsPerAssemblyDto> releaseStatsPerAssemblyDtos = new ArrayList();
        for (ReleaseStatsPerAssembly assembly : releaseStatsPerAssembly) {
            releaseStatsPerAssemblyDtos.add(toDto(assembly));
        }
        return releaseStatsPerAssemblyDtos;
    }

    private ReleaseStatsPerAssemblyDto toDto(ReleaseStatsPerAssembly releaseStatsPerAssembly) {
        Map<Integer, String> releasesFtp = this.releaseStatMapperUtils.getReleasesFtp();

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
        releaseStatsPerAssemblyDto.setClusteredCurrentRs(releaseStatsPerAssembly.getClusteredCurrentRs());
        releaseStatsPerAssemblyDto.setNewClusteredCurrentRs(releaseStatsPerAssembly.getNewClusteredCurrentRs());
        String releaseLink = releasesFtp.get(releaseStatsPerAssembly.getReleaseVersion()) + SPECIES_DIRECTORY +
                releaseStatsPerAssembly.getReleaseFolder();
        releaseStatsPerAssemblyDto.setReleaseLink(releaseLink);
        releaseStatsPerAssemblyDto.setTaxonomyLink(TAXONOMY_URL + releaseStatsPerAssembly.getTaxonomyId());
        return releaseStatsPerAssemblyDto;
    }

}
