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
package uk.ac.ebi.eva.stats.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.stats.models.ReleaseStatsPerSpecies;
import uk.ac.ebi.eva.stats.services.ReleaseStatsService;

@RestController
@RequestMapping(value = "/v1/stats", produces = "application/json")
public class ReleaseStatsController {

    private final ReleaseStatsService releaseStatsService;

    public ReleaseStatsController(ReleaseStatsService releaseStatsService) {
        this.releaseStatsService = releaseStatsService;
    }

    @GetMapping("/per-species")
    public Iterable<ReleaseStatsPerSpecies> getReleaseStatsPerSpecies(
            @RequestParam(name = "releaseVersion", required = false) Integer releaseVersion,
            @RequestParam(name = "excludeUnmappedOnly", required = false) boolean excludeUnmappedOnly) {
        return releaseStatsService.getReleaseStatsPerSpecies(releaseVersion, excludeUnmappedOnly);

    }
}
