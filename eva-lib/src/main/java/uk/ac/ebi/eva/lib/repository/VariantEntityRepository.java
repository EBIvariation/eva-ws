/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2016 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.lib.repository;

import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.Variant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;
import uk.ac.ebi.eva.lib.filter.VariantEntityRepositoryFilter;

import java.util.List;

/**
 * Spring MongoRepository for VariantEntity class.
 *
 * Methods include querying by id, and by region.
 */
public interface VariantEntityRepository extends MongoRepository<VariantEntity, String>, VariantEntityRepositoryCustom {

    enum RelationalOperator { EQ, GT, LT, GTE, LTE, IN }

    List<VariantEntity> findByIdsAndComplexFilters(String id, List<VariantEntityRepositoryFilter> filters,
                                                   List<String> exclude,
                                                   Pageable pageable);

    Long countByIdsAndComplexFilters(String id, List<VariantEntityRepositoryFilter> filters);

    List<VariantEntity> findByRegionsAndComplexFilters(List<Region> regions,
                                                       List<VariantEntityRepositoryFilter> filters,
                                                       List<String> exclude, Pageable pageable);

    Long countByRegionsAndComplexFilters(List<Region> regions, List<VariantEntityRepositoryFilter> filters);

    @Query("{'chr': ?0, 'start': ?1, 'ref': ?2, 'alt': ?3}")
    List<VariantEntity> findByChromosomeAndStartAndReferenceAndAlternate(String chromosome, int start,
                                                                         String reference, String alternate);

    @Query("{'chr': ?0, 'start': ?1, 'ref': ?2, 'alt': ?3, 'files.sid': {$in : ?4}}")
    List<VariantEntity> findByChromosomeAndStartAndReferenceAndAlternateAndStudyIn(String chromosome, int start,
                                                                                   String reference, String alternate,
                                                                                   List<String> studyIds);

    @Query("{'chr': ?0, 'start': ?1, 'ref': ?2, 'files.sid': {$in : ?3}}")
    List<VariantEntity> findByChromosomeAndStartAndReferenceAndStudyIn(String chromosome, int start, String reference,
                                                                       List<String> studyIds);

    List<String> findDistinctChromosomes();

    @Query("{'chr': ?0, 'start': ?1, 'ref': ?2}")
    List<VariantEntity> findByChromosomeAndStartAndReference(String chr, int start, String ref);

    @Query(value = "{'chr': ?0, 'start': ?1, 'alt': ?2, 'files.sid': {$in : ?3}}}")
    List<VariantEntity> findByChromosomeAndStartAndAltAndStudyIn(String chr, int start, String alt,
                                                                 List<String> studyIds);

    @Query(value = "{'chr': ?0, 'start': ?1, 'type': ?2, 'files.sid': {$in : ?3}}}")
    List<VariantEntity> findByChromosomeAndStartAndTypeAndStudyIn(String chr, int start, Variant.VariantType type,
                                                                  List<String> studyIds);
}
