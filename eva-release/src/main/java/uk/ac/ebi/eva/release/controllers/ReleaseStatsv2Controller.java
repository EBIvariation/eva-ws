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
package uk.ac.ebi.eva.release.controllers;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.eva.release.dto.ReleaseStatsPerAssemblyDto;
import uk.ac.ebi.eva.release.dto.ReleaseStatsPerSpeciesDto;
import uk.ac.ebi.eva.release.dto.ReleaseStatsPerSpeciesV2Dto;
import uk.ac.ebi.eva.release.services.ReleaseStatsServicev2;

@RestController
@RequestMapping(value = "/v1/stats2", produces = "application/json")
@Api(tags = {"RS Release Statistics"})
public class ReleaseStatsv2Controller {

    private final ReleaseStatsServicev2 releaseStatsService;

    public ReleaseStatsv2Controller(ReleaseStatsServicev2 releaseStatsService) {
        this.releaseStatsService = releaseStatsService;
    }

    @GetMapping("/per-species")
    public Iterable<ReleaseStatsPerSpeciesV2Dto> getReleaseStatsPerSpecies(
            @RequestParam(name = "releaseVersion", required = false) Integer releaseVersion,
            @RequestParam(name = "excludeUnmappedOnly", required = false) boolean excludeUnmappedOnly) {
        return releaseStatsService.getReleaseStatsPerSpecies(releaseVersion);
    }

//    @GetMapping("/per-species/new")
//    public Iterable<ReleaseStatsPerSpeciesDto> getSpeciesWithNewRsIds(
//            @RequestParam(name = "releaseVersion") Integer releaseVersion) {
//        return releaseStatsService.getSpeciesWithNewRsIds(releaseVersion);
//    }
//
//    @GetMapping("/per-assembly")
//    public Iterable<ReleaseStatsPerAssemblyDto> getReleaseStatsPerAssemblies(
//            @RequestParam(name = "releaseVersion", required = false) Integer releaseVersion) {
//        return releaseStatsService.getReleaseStatsPerAssembly(releaseVersion);
//    }
//
//    @GetMapping("/per-assembly/new")
//    public Iterable<ReleaseStatsPerAssemblyDto> getAssembliesWithNewRsIds(
//            @RequestParam(name = "releaseVersion") Integer releaseVersion) {
//        return releaseStatsService.getReleaseStatsPerAssemblyWithNewRsIds(releaseVersion);
//    }
}
