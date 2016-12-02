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
package uk.ac.ebi.eva.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;

import java.util.List;

/**
 * Spring MongoRepository for VariantEntity class.
 *
 * Methods include querying by id, and by region.
 */
interface VariantEntityRepository extends MongoRepository<VariantEntity, String>, VariantEntityRepositoryCustom {

    List<VariantEntity> findByIds(String id);

    /**
     * Method that queries for variants within a specified range, and has values within specified bounds or
     * have specified values matching those in the filters- which include consequence type, minor allele frequency,
     * protein substitution scores (polyphen and sift), and studies.
     *
     * @param chr Chromosome name
     * @param start Start position of query, inclusive
     * @param end End position of query, inclusive
     * @param consequenceType List of genomic consequences, returned variants have at least one consequence type
     *                        from in the list.
     * @param maf Filter for minor allele frequency value
     * @param polyphenScore Filter for polyphen score, which predicts the possible impact of an amino acid
     *                      substitution on the structure and function of a human protein.
     * @param sift Filter for SIFT score, which predicts whether an amino acid substitution affects protein function.
     * @param studies List of bioproject IDs, returned variants were found in at least one of the studies in
     *                this list.
     * @return VariantEntities whose values are within the bounds of the filters.
     */
    List<VariantEntity> findByRegionAndComplexFilters(String chr, int start, int end,
                                                      List<String> consequenceType, String maf,
                                                      String polyphenScore, String sift,
                                                      List<String> studies);
}
