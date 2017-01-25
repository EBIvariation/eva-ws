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
import org.springframework.data.domain.Pageable;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;
import uk.ac.ebi.eva.lib.repository.VariantEntityRepository.RelationalOperator;
import uk.ac.ebi.eva.lib.utils.RepositoryFilter;

import java.util.List;

/**
 * Interface to declare additional repository methods with a custom implementation,
 * instead of the one that Spring Data would provide by default.
 */
interface VariantEntityRepositoryCustom {

    /**
     * Query for variants with a specified ID (eg. RS IDs), and whose attributes match those values specified in the
     * filters: study, consequence type, minor allele frequency and protein substitution scores (Polyphen and SIFT).
     *
     * @param id Variant id
     * @param studies List of study IDs, returned variants must be found in at least one of them
     * @param consequenceType List of genomic consequences, returned variants must have at least one of them
     * @param mafOperator Relational operator for querying of variants by maf value
     * @param mafValue Filter for minor allele frequency value
     * @param polyphenScoreOperator Relational operator for querying of variants by polyphen value
     * @param polyphenScoreValue Filter for Polyphen score, which predicts the possible impact of an amino acid
     *                      substitution on the structure and function of a human protein
     * @param siftScoreOperator Relational operator for querying of variants by sift value
     * @param siftScoreValue Filter for SIFT score, which predicts if an amino acid substitution affects protein function
     * @param exclude List of strings, each matching a field in the variant Mongo documents. Fields specified in the
     *                list will be excluded from the returned document(s)
     * @return VariantEntities whose values are within the bounds of the filters
     */
    List<VariantEntity> findByIdsAndComplexFilters(String id, List<RepositoryFilter> filters, List<String> exclude, Pageable pageable);

    Long countByIdsAndComplexFilters(String id, List<RepositoryFilter> filters);

    /**
     * Query for variants within a set of specified genomic regions, and whose attributes match those values specified
     * in the filters: study, consequence type, minor allele frequency and protein substitution scores (Polyphen and
     * SIFT).
     *
     * @param regions List of region objects to invlude in query
     * @param studies List of study IDs, returned variants must be found in at least one of them
     * @param consequenceType List of genomic consequences, returned variants have at least one consequence type
     *                        from in the list.
     * @param mafOperator Relational operator for querying of variants by maf value
     * @param mafValue Filter for minor allele frequency value
     * @param polyphenScoreOperator Relational operator for querying of variants by polyphen value
     * @param polyphenScoreValue Filter for polyphen score, which predicts the possible impact of an amino acid
     *                      substitution on the structure and function of a human protein
     * @param siftScoreOperator Relational operator for querying of variants by sift value
     * @param siftScoreValue Filter for SIFT score, which predicts if an amino acid substitution affects protein function
     * @param exclude List of strings, each matching a field in the variant Mongo documents. Fields specified in the
     *                list will be excluded from the returned document(s)
     * @return VariantEntities whose values are within the bounds of the filters
     */
    List<VariantEntity> findByRegionsAndComplexFilters(List<Region> regions, List<RepositoryFilter> filters,
                                                       List<String> exclude, Pageable pageable);

    Long countByRegionsAndComplexFilters(List<Region> regions, List<RepositoryFilter> filters);

}
