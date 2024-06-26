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

import uk.ac.ebi.eva.release.repositories.ReleaseInfoRepository;

import java.util.HashMap;
import java.util.Map;


public class ReleaseStatsMapperUtils {

    private final ReleaseInfoRepository releaseInfoRepository;

    public ReleaseStatsMapperUtils(ReleaseInfoRepository releaseInfoRepository) {
        this.releaseInfoRepository = releaseInfoRepository;
    }

    public Map<Integer, String> getReleasesFtp() {
        Map<Integer, String> releaseFtp = new HashMap<>();
        releaseInfoRepository.findAll().forEach(r -> releaseFtp.put(r.getReleaseVersion(), r.getReleaseFtp()));
        return releaseFtp;
    }

}
