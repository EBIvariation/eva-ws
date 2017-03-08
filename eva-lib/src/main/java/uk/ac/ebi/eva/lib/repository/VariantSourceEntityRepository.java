/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.lib.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import uk.ac.ebi.eva.commons.models.data.VariantSourceEntity;

import java.util.List;

/**
 * Extension of Spring's MongoRepository for VariantSourceEntity class.
 *
 * This interface queries the VariantSourceEntity collection- i.e. collection containing information on files.
 *
 * Methods include: finding all "VariantSourceEntity"s in the collection, finding "VariantSourceEntity"s with either
 * studyId matching given value, or studyName matching given value.
 *
 */
public interface VariantSourceEntityRepository extends MongoRepository<VariantSourceEntity, String> {

    List<VariantSourceEntity> findAll();

    List<VariantSourceEntity> findByStudyIdOrStudyName(String studyId, String studyName);

}
