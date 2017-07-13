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
package uk.ac.ebi.eva.lib.utils;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import uk.ac.ebi.eva.commons.core.models.StudyType;
import uk.ac.ebi.eva.lib.extension.GenericSpecifications;
import uk.ac.ebi.eva.lib.repositories.EvaStudyBrowserRepository;

import static org.springframework.data.jpa.domain.Specifications.where;

public class EvaproDbUtils {

    public static StudyType stringToStudyType(String studyType) {
        switch (studyType) {
            case "Collection":
            case "Curated Collection":
                return StudyType.COLLECTION;
            case "Control Set":
            case "Control-Set":
                return StudyType.CONTROL;
            case "Case Control":
            case "Case-Control":
                return StudyType.CASE_CONTROL;
            case "Case Set":
            case "Case-Set":
                return StudyType.CASE;
            case "Tumor vs. Matched-Normal":
                return StudyType.PAIRED_TUMOR;
            case "Aggregate":
                return StudyType.AGGREGATE;
            default:
                throw new IllegalArgumentException("Study type " + studyType + " is not valid");
        }
    }

    public static Specification getSpeciesAndTypeFilters(QueryOptions queryOptions) {
        if (!queryOptions.containsKey(QueryOptionsConstants.SPECIES) && !queryOptions.containsKey(QueryOptionsConstants.TYPE)) {
            return null;
        }

        Specifications speciesSpecifications = null;
        if (queryOptions.containsKey(QueryOptionsConstants.SPECIES)) {
            Object[] species = queryOptions.getAsStringList(QueryOptionsConstants.SPECIES).toArray(new Object[]{});
            speciesSpecifications = where(GenericSpecifications.in(EvaStudyBrowserRepository.COMMON_NAME, species))
                    .or(GenericSpecifications.in(EvaStudyBrowserRepository.SCIENTIFIC_NAME, species));
        }

        Specifications typeSpecifications = null;
        if (queryOptions.containsKey(QueryOptionsConstants.TYPE)) {
            Object[] types = queryOptions.getAsStringList(QueryOptionsConstants.TYPE).toArray(new Object[]{});
            typeSpecifications = where(GenericSpecifications.in(EvaStudyBrowserRepository.EXPERIMENT_TYPE, types));
            for (Object type : types) {
                typeSpecifications = typeSpecifications.or(
                        GenericSpecifications.like(EvaStudyBrowserRepository.EXPERIMENT_TYPE, "%" + type + "%"));
            }
        }

        if (speciesSpecifications != null && typeSpecifications != null) {
            return speciesSpecifications.and(typeSpecifications);
        } else {
            if (speciesSpecifications != null) {
                return speciesSpecifications;
            } else {
                return typeSpecifications;
            }
        }
    }

}
