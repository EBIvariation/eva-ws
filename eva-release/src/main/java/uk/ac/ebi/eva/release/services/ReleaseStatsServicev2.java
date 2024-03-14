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
import uk.ac.ebi.eva.release.dto.ReleaseStatsPerSpeciesV2Dto;
import uk.ac.ebi.eva.release.mappers.ReleaseStatsPerAssemblyMapper;
import uk.ac.ebi.eva.release.mappers.ReleaseStatsPerSpeciesMapper;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerTaxonomyView;
import uk.ac.ebi.eva.release.repositories.ReleaseStatsPerAssemblyViewRepository;
import uk.ac.ebi.eva.release.repositories.ReleaseStatsPerTaxonomyViewRepository;

import java.util.HashMap;

@Service
public class ReleaseStatsServicev2 {

    private final ReleaseStatsPerTaxonomyViewRepository releaseStatsPerTaxonomyRepository;

    private final ReleaseStatsPerAssemblyViewRepository releaseStatsPerAssemblyRepository;

    private final ReleaseStatsPerSpeciesMapper releaseStatsPerSpeciesMapper;

    private final ReleaseStatsPerAssemblyMapper releaseStatsPerAssemblyMapper;

    public ReleaseStatsServicev2(ReleaseStatsPerTaxonomyViewRepository releaseStatsPerTaxonomyRepository,
                                 ReleaseStatsPerAssemblyViewRepository releaseStatsPerAssemblyRepository,
                                 ReleaseStatsPerSpeciesMapper releaseStatsPerSpeciesMapper,
                                 ReleaseStatsPerAssemblyMapper releaseStatsPerAssemblyMapper) {
        this.releaseStatsPerTaxonomyRepository = releaseStatsPerTaxonomyRepository;
        this.releaseStatsPerAssemblyRepository = releaseStatsPerAssemblyRepository;
        this.releaseStatsPerSpeciesMapper = releaseStatsPerSpeciesMapper;
        this.releaseStatsPerAssemblyMapper = releaseStatsPerAssemblyMapper;
    }

    public Iterable<ReleaseStatsPerSpeciesV2Dto> getReleaseStatsPerSpecies(Integer releaseVersion) {
        Iterable<ReleaseStatsPerTaxonomyView> releaseData;
        releaseData = releaseStatsPerTaxonomyRepository.findAllByReleaseVersion(releaseVersion);
        HashMap<String, ReleaseStatsPerSpeciesV2Dto> keyToDto= new HashMap<>();
        for (ReleaseStatsPerTaxonomyView viewData : releaseData){
            String key = viewData.getTaxonomyId() + "_" + viewData.getReleaseVersion();
            if (! keyToDto.containsKey(key)){
                keyToDto.put(key, new ReleaseStatsPerSpeciesV2Dto());
            }
            ReleaseStatsPerSpeciesV2Dto dto = keyToDto.get(key);
            dto.setTaxonomyId(viewData.getTaxonomyId());
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
        return keyToDto.values();
    }

    private Iterable<ReleaseStatsPerSpeciesV2Dto> getReleaseDataByVersionExcludingUnmappedOnly(Integer releaseVersion) {
        return getReleaseStatsPerSpecies(releaseVersion);
    }

    private Iterable<ReleaseStatsPerSpeciesV2Dto> getReleaseDataExcludingUnmappedOnly() {
        return getReleaseStatsPerSpecies(null);
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
//    public Iterable<ReleaseStatsPerAssemblyDto> getReleaseStatsPerAssembly(Integer releaseVersion) {
//        Iterable<ReleaseStatsPerAssembly> releaseData;
//        if (releaseVersion != null) {
//            releaseData = releaseStatsPerAssemblyRepository.findAllByReleaseVersion(releaseVersion);
//        } else {
//            releaseData = releaseStatsPerAssemblyRepository.findAll();
//        }
//        return releaseStatsPerAssemblyMapper.toDto(releaseData);
//    }
//
//    public Iterable<ReleaseStatsPerAssemblyDto> getReleaseStatsPerAssemblyWithNewRsIds(Integer releaseVersion) {
//        if (releaseVersion != null) {
//            return releaseStatsPerAssemblyMapper.toDto(
//                    releaseStatsPerAssemblyRepository.findByReleaseVersionAndNewCurrentRsGreaterThan(releaseVersion, 0L));
//        } else {
//            return releaseStatsPerAssemblyMapper.toDto(
//                    releaseStatsPerAssemblyRepository.findByNewCurrentRsGreaterThan(0L));
//        }
//    }
}
