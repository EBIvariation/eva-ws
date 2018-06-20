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
package uk.ac.ebi.eva.lib.dgva_utils;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import uk.ac.ebi.eva.lib.repositories.DgvaStudyBrowserRepository;
import uk.ac.ebi.eva.lib.utils.QueryOptions;
import uk.ac.ebi.eva.lib.utils.QueryOptionsConstants;

import static org.springframework.data.jpa.domain.Specifications.where;
import static uk.ac.ebi.eva.lib.extension.GenericSpecifications.in;
import static uk.ac.ebi.eva.lib.extension.GenericSpecifications.like;

public class DgvaDBUtils {

    public static Specification getSpeciesAndTypeFilters(QueryOptions queryOptions) {
        if (!queryOptions.containsKey(QueryOptionsConstants.SPECIES) && !queryOptions.containsKey(QueryOptionsConstants.TYPE)) {
            return null;
        }

        Specifications speciesSpecifications = null;
        if (queryOptions.containsKey(QueryOptionsConstants.SPECIES)) {
            String[] species = queryOptions.getAsStringList(QueryOptionsConstants.SPECIES).toArray(new String[]{});
            speciesSpecifications = where(in(DgvaStudyBrowserRepository.COMMON_NAME, (Object[])species))
                    .or(in(DgvaStudyBrowserRepository.SCIENTIFIC_NAME, (Object[])species));

            for (String speciesName : species) {
                speciesSpecifications = speciesSpecifications
                        .or(like(DgvaStudyBrowserRepository.COMMON_NAME, "%" + speciesName + "%"))
                        .or(like(DgvaStudyBrowserRepository.SCIENTIFIC_NAME, "%" + speciesName + "%"));
            }
        }

        Specifications typeSpecifications = null;
        if (queryOptions.containsKey(QueryOptionsConstants.TYPE)) {
            String[] types = queryOptions.getAsStringList(QueryOptionsConstants.TYPE).toArray(new String[]{});
            typeSpecifications = where(in(DgvaStudyBrowserRepository.STUDY_TYPE, (Object[])types));
            for (String type : types) {
                typeSpecifications = typeSpecifications.or(like(DgvaStudyBrowserRepository.STUDY_TYPE, "%" + type + "%"));
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
