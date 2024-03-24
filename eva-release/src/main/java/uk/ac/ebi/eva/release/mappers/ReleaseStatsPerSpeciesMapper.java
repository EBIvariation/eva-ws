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
import uk.ac.ebi.eva.release.dto.ReleaseStatsPerSpeciesV2Dto;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerSpecies;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerTaxonomyV2;
import uk.ac.ebi.eva.release.repositories.ReleaseInfoRepository;

import java.util.*;

@Component
public class ReleaseStatsPerSpeciesMapper {

    private static final String SPECIES_DIRECTORY = "by_species/";

    private static final String TAXONOMY_URL = "https://www.ebi.ac.uk/ena/browser/view/Taxon:";


    private final Map<Integer, String> releaseLinkMap;

    private final ReleaseStatsMapperUtils releaseStatMapperUtils;

    public ReleaseStatsPerSpeciesMapper(ReleaseInfoRepository releaseInfoRepository) {
        this.releaseStatMapperUtils = new ReleaseStatsMapperUtils(releaseInfoRepository);
        this.releaseLinkMap = this.releaseStatMapperUtils.getReleasesFtp();
    }

    public Collection<ReleaseStatsPerSpeciesV2Dto> toDtoV2(Iterable<ReleaseStatsPerTaxonomyV2> releaseData){
        HashMap<String, ReleaseStatsPerSpeciesV2Dto> keyToDto= new HashMap<>();
        for (ReleaseStatsPerTaxonomyV2 taxonomyData : releaseData) {
            String key = taxonomyData.getKey();
            if (!keyToDto.containsKey(key)) {
                keyToDto.put(key, new ReleaseStatsPerSpeciesV2Dto());
            }
            ReleaseStatsPerSpeciesV2Dto dto = keyToDto.get(key);
            dto.setTaxonomyId(taxonomyData.getTaxonomyId());
            dto.setScientificName(taxonomyData.getScientificName());
            dto.setCommonName(taxonomyData.getCommonName());
            this.releaseStatMapperUtils.populateDtoFromV2LongForm(dto, taxonomyData);
            dto.setReleaseFolder(taxonomyData.getReleaseFolder());
            String releaseLink = this.releaseLinkMap.get(taxonomyData.getReleaseVersion()) + SPECIES_DIRECTORY +
                    taxonomyData.getReleaseFolder();
            dto.setReleaseLink(releaseLink);
            dto.setAssemblyAccessions(taxonomyData.getAssemblyAccessions());
            dto.setTaxonomyLink(TAXONOMY_URL + taxonomyData.getTaxonomyId());
        }
        return keyToDto.values();
    }

    public Iterable<ReleaseStatsPerSpeciesDto> toDto(Iterable<ReleaseStatsPerSpecies> releaseStatsPerSpecies) {
        List<ReleaseStatsPerSpeciesDto> releaseStatsPerSpeciesDtos = new ArrayList();
        for (ReleaseStatsPerSpecies species : releaseStatsPerSpecies) {
            releaseStatsPerSpeciesDtos.add(toDto(species));
        }
        return releaseStatsPerSpeciesDtos;
    }

    private ReleaseStatsPerSpeciesDto toDto(ReleaseStatsPerSpecies releaseStatsPerSpecies) {
        Map<Integer, String> releasesFtp = this.releaseStatMapperUtils.getReleasesFtp();

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

}
