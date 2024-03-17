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
import uk.ac.ebi.eva.release.dto.ReleaseStatsPerAssemblyV2Dto;
import uk.ac.ebi.eva.release.dto.ReleaseStatsPerSpeciesV2Dto;
import uk.ac.ebi.eva.release.dto.ReleaseStatsPerV2Dto;
import uk.ac.ebi.eva.release.mappers.ReleaseStatsPerAssemblyMapper;
import uk.ac.ebi.eva.release.mappers.ReleaseStatsPerSpeciesMapper;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerAssembly;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerAssemblyView;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerTaxonomyView;
import uk.ac.ebi.eva.release.models.ReleaseStatsView;
import uk.ac.ebi.eva.release.repositories.ReleaseStatsPerAssemblyViewRepository;
import uk.ac.ebi.eva.release.repositories.ReleaseStatsPerTaxonomyViewRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ReleaseStatsServicev2 {

    private final ReleaseStatsPerTaxonomyViewRepository releaseStatsPerTaxonomyRepository;

    private final ReleaseStatsPerAssemblyViewRepository releaseStatsPerAssemblyRepository;


    public ReleaseStatsServicev2(ReleaseStatsPerTaxonomyViewRepository releaseStatsPerTaxonomyRepository,
                                 ReleaseStatsPerAssemblyViewRepository releaseStatsPerAssemblyRepository) {
        this.releaseStatsPerTaxonomyRepository = releaseStatsPerTaxonomyRepository;
        this.releaseStatsPerAssemblyRepository = releaseStatsPerAssemblyRepository;
    }

    public Iterable<ReleaseStatsPerSpeciesV2Dto> getReleaseStatsPerSpecies(boolean excludeUnmappedOnly) {
        Iterable<ReleaseStatsPerTaxonomyView> releaseData;
        releaseData = releaseStatsPerTaxonomyRepository.findAll();
        return populateAllTaxonomyDtoFrom(releaseData, excludeUnmappedOnly, false);
    }

    public Iterable<ReleaseStatsPerSpeciesV2Dto> getReleaseStatsPerSpecies(Integer releaseVersion, boolean excludeUnmappedOnly) {
        Iterable<ReleaseStatsPerTaxonomyView> releaseData;
        releaseData = releaseStatsPerTaxonomyRepository.findAllByReleaseVersion(releaseVersion);
        return populateAllTaxonomyDtoFrom(releaseData, excludeUnmappedOnly, false);
    }

    public Iterable<ReleaseStatsPerSpeciesV2Dto> getSpeciesWithNewRsIds(Integer releaseVersion){
        Iterable<ReleaseStatsPerTaxonomyView> releaseData;
        releaseData = releaseStatsPerTaxonomyRepository.findAllByReleaseVersion(releaseVersion);
        return populateAllTaxonomyDtoFrom(releaseData, false, true);

    }

    private Iterable<ReleaseStatsPerSpeciesV2Dto> populateAllTaxonomyDtoFrom(
            Iterable<ReleaseStatsPerTaxonomyView> releaseData,
            boolean excludeUnmappedOnly,
            boolean excludeNonNew
    ){
        HashMap<String, ReleaseStatsPerSpeciesV2Dto> keyToDto= new HashMap<>();
        for (ReleaseStatsPerTaxonomyView viewData : releaseData) {
            String key = viewData.getKey();
            if (!keyToDto.containsKey(key)) {
                keyToDto.put(key, new ReleaseStatsPerSpeciesV2Dto());
            }
            ReleaseStatsPerSpeciesV2Dto dto = keyToDto.get(key);
            dto.setTaxonomyId(viewData.getTaxonomyId());
            populateDtoFromViewData(dto, viewData);
        }
        return keyToDto.values().stream()
                .filter(excludeUnmappedOnly? this::isNotUnmappedOnly : s -> true)
                .filter(excludeNonNew? this::isNonNew :s -> true)
                .collect(Collectors.toList());
    }

    private Iterable<ReleaseStatsPerAssemblyV2Dto> populateAssemblyDtoFrom(
            Iterable<ReleaseStatsPerAssemblyView> releaseData,
            boolean excludeUnmappedOnly,
            boolean excludeNonNew
    ){
        HashMap<String, ReleaseStatsPerAssemblyV2Dto> keyToDto= new HashMap<>();
        for (ReleaseStatsPerAssemblyView viewData : releaseData) {
            String key = viewData.getKey();
            if (!keyToDto.containsKey(key)) {
                keyToDto.put(key, new ReleaseStatsPerAssemblyV2Dto());
            }
            ReleaseStatsPerAssemblyV2Dto dto = keyToDto.get(key);
            dto.setAssemblyAccession(viewData.getAssemblyAccession());
            populateDtoFromViewData(dto, viewData);
        }
        return keyToDto.values().stream()
                .filter(excludeUnmappedOnly? this::isNotUnmappedOnly : s -> true)
                .filter(excludeNonNew? this::isNonNew :s -> true)
                .collect(Collectors.toList());
    }
    private boolean isNotUnmappedOnly(ReleaseStatsPerV2Dto dto){
        return dto.getCurrentRs() != 0 &&
                dto.getMergedRs() != 0 &&
                dto.getDeprecatedRs() != 0 &&
                dto.getMergedDeprecatedRs() !=0;
    }
    private boolean isNonNew(ReleaseStatsPerV2Dto dto){
        return dto.getNewCurrentRs() > 0 ||
                dto.getNewMergedRs() > 0 ||
                dto.getNewDeprecatedRs() > 0 ||
                dto.getNewMultiMappedRs() > 0 ||
                dto.getNewUnmappedRs() > 0;
    }

    private void populateDtoFromViewData(ReleaseStatsPerV2Dto dto, ReleaseStatsView viewData){
        dto.setReleaseVersion(viewData.getReleaseVersion());
        switch (viewData.getRsType()){
            case "current":
                dto.setCurrentRs(viewData.getCount());
                dto.setNewCurrentRs(viewData.getNewAddition());
                break;
            case "deprecated":
                dto.setDeprecatedRs(viewData.getCount());
                dto.setNewDeprecatedRs(viewData.getNewAddition());
                break;
            case "merged":
                dto.setMergedRs(viewData.getCount());
                dto.setNewMergedRs(viewData.getNewAddition());
                break;
            case "merged_deprecated":
                dto.setMergedDeprecatedRs(viewData.getCount());
                dto.setNewMergedDeprecatedRs(viewData.getNewAddition());
                break;
            case "multimap":
                dto.setMultiMappedRs(viewData.getCount());
                dto.setNewMultiMappedRs(viewData.getNewAddition());
                break;
            case "unmapped":
                dto.setUnmappedRs(viewData.getCount());
                dto.setNewUnmappedRs(viewData.getNewAddition());
                break;
        }
    }


//    public Iterable<ReleaseStatsPerSpeciesV2Dto> getSpeciesWithNewRsIds(Integer releaseVersion) {
//        if (releaseVersion != null) {
//            return releaseStatsPerSpeciesMapper.toDto(releaseStatsPerSpeciesRepository
//                                 .findByReleaseVersionAndNewCurrentRsGreaterThan(releaseVersion, 0L));
//        } else {
//            return releaseStatsPerSpeciesMapper.toDto(releaseStatsPerSpeciesRepository.findByNewCurrentRsGreaterThan(0L));
//        }
//    }
//
    public Iterable<ReleaseStatsPerAssemblyV2Dto> getReleaseStatsPerAssembly(Integer releaseVersion, boolean excludeNonNew) {
        Iterable<ReleaseStatsPerAssemblyView> releaseData;
        if (releaseVersion != null) {
            releaseData = releaseStatsPerAssemblyRepository.findAllByReleaseVersion(releaseVersion);
        } else {
            releaseData = releaseStatsPerAssemblyRepository.findAll();
        }
        return populateAssemblyDtoFrom(releaseData, false, excludeNonNew);
    }

}
