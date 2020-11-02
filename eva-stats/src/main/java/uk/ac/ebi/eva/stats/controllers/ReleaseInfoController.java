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

import uk.ac.ebi.eva.stats.models.ReleaseInfo;
import uk.ac.ebi.eva.stats.repositories.ReleaseInfoRepository;

import java.util.Collections;

@RestController
@RequestMapping(value = "/v1/info", produces = "application/json")
public class ReleaseInfoController {

    private final ReleaseInfoRepository releaseInfoRepository;

    public ReleaseInfoController(ReleaseInfoRepository releaseInfoRepository) {
        this.releaseInfoRepository = releaseInfoRepository;
    }

    @GetMapping
    public Iterable<ReleaseInfo> getReleaseInfo(
            @RequestParam(name = "releaseVersion", required = false) Integer releaseVersion) {
        if (releaseVersion != null) {
            return Collections.singleton(releaseInfoRepository.findById(releaseVersion).get());
        } else {
            return releaseInfoRepository.findAll();
        }
    }
}
