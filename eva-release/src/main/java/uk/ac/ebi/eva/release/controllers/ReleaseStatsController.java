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
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.release.dto.ReleaseStatsPerSpeciesDto;
import uk.ac.ebi.eva.release.services.ReleaseStatsService;

@RestController
@RequestMapping(value = "/v1/stats", produces = "application/json")
@Api(tags = {"RS Release Statistics"})
public class ReleaseStatsController {

    private final ReleaseStatsService releaseStatsService;

    public ReleaseStatsController(ReleaseStatsService releaseStatsService) {
        this.releaseStatsService = releaseStatsService;
    }

    @GetMapping("/per-species")
    public Iterable<ReleaseStatsPerSpeciesDto> getReleaseStatsPerSpecies(
            @ApiParam(value = "Version of the RS Release, e.g.: 2")
            @RequestParam(name = "releaseVersion", required = false) Integer releaseVersion,
            @ApiParam(value = "Flag to indicate if unmapped variants should be excluded from statistics")
            @RequestParam(name = "excludeUnmappedOnly", required = false) boolean excludeUnmappedOnly) {
        return releaseStatsService.getReleaseStatsPerSpecies(releaseVersion, excludeUnmappedOnly);
    }

    @GetMapping("/per-species/new")
    public Iterable<ReleaseStatsPerSpeciesDto> getSpeciesWithNewRsIds(
            @ApiParam(value = "Version of the RS Release, e.g.: 2")
            @RequestParam(name = "releaseVersion") Integer releaseVersion) {
        return releaseStatsService.getSpeciesWithNewRsIds(releaseVersion);
    }
}
