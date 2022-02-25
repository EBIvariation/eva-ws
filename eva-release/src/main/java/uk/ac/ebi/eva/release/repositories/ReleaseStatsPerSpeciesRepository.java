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
package uk.ac.ebi.eva.release.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.release.models.ReleaseStatsPerSpeciesPK;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerSpecies;

@Repository
public interface ReleaseStatsPerSpeciesRepository extends CrudRepository<ReleaseStatsPerSpecies,
        ReleaseStatsPerSpeciesPK> {

    Iterable<ReleaseStatsPerSpecies> findAllByReleaseVersion(int releaseVersion);

    //All release data excluding rows with only unmapped data
    Iterable<ReleaseStatsPerSpecies> findByCurrentRsNotAndMultiMappedRsNotAndMergedRsNotAndDeprecatedRsNotAndMergedDeprecatedRsNotAndUnmappedRsGreaterThan(
            long currentRs, long multiMappedRs, long mergedRs, long deprecatedRs, long mergedDeprecatedRs,
            long unmappedRs);

    //Data by release version excluding rows with only unmapped data
    Iterable<ReleaseStatsPerSpecies> findByReleaseVersionAndCurrentRsNotAndMultiMappedRsNotAndMergedRsNotAndDeprecatedRsNotAndMergedDeprecatedRsNotAndUnmappedRsGreaterThan(
            int releaseVersion, long currentRs, long multiMappedRs, long mergedRs, long deprecatedRs,
            long mergedDeprecatedRs, long unmappedRs);

    Iterable<ReleaseStatsPerSpecies> findByNewCurrentRsGreaterThan(long currentRs);

    Iterable<ReleaseStatsPerSpecies> findByReleaseVersionAndNewCurrentRsGreaterThan(int releaseVersion, long currentRs);
}
