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

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.release.models.ReleaseStartsPerSpeciesPK;
import uk.ac.ebi.eva.release.models.ReleaseStatsPerSpecies;

@Repository
public interface ReleaseStatsPerSpeciesRepository extends CrudRepository<ReleaseStatsPerSpecies,
        ReleaseStartsPerSpeciesPK> {

    Iterable<ReleaseStatsPerSpecies> findAllByReleaseVersion(int releaseVersion);

    //All released species stats excluding the unmapped only
    @Query(value = "select * from release_rs_statistics_per_species " +
            "where not (current_rs = 0 and multi_mapped_rs = 0 and merged_rs = 0 and deprecated_rs = 0 " +
            "and merged_deprecated_rs = 0 and unmapped_rs > 0)", nativeQuery=true)
    Iterable<ReleaseStatsPerSpecies> getAllExcludingUnmappedOnly();

    //Species stats by release version excluding the unmapped only
    @Query(value = "select * from release_rs_statistics_per_species " +
            "where release_version = ?1 and not (current_rs = 0 and multi_mapped_rs = 0 and merged_rs = 0 " +
            "and deprecated_rs = 0  and merged_deprecated_rs = 0 and unmapped_rs > 0)", nativeQuery=true)
    Iterable<ReleaseStatsPerSpecies> getAllByVersionExcludingUnmappedOnly(int releaseVersion);

    //Species that introduced new variants in specified release
    @Query(value = "select * from release_rs_statistics_per_species where release_version = ?1 " +
            "and (new_current_rs > 0 or new_merged_rs > 0 or new_deprecated_rs > 0 or new_merged_deprecated_rs > 0 " +
            "or new_unmapped_rs > 0)", nativeQuery=true)
    Iterable<ReleaseStatsPerSpecies> getSpeciesWithVariantsInRelease(int releaseVersion);
}
