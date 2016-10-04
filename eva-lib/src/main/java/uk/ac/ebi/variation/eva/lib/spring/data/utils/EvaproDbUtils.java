package uk.ac.ebi.variation.eva.lib.spring.data.utils;

import org.opencb.datastore.core.QueryOptions;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import uk.ac.ebi.variation.eva.lib.spring.data.repository.StudyBrowserRepository;

import static org.springframework.data.jpa.domain.Specifications.where;
import static uk.ac.ebi.variation.eva.lib.spring.data.extension.GenericSpecifications.in;
import static uk.ac.ebi.variation.eva.lib.spring.data.extension.GenericSpecifications.like;

/**
 * Created by jorizci on 04/10/16.
 */
public class EvaproDbUtils {

    public static Specification getSpeciesAndTypeFilters(QueryOptions queryOptions) {
        if (!queryOptions.containsKey(QueryOptionsConstants.SPECIES) && !queryOptions.containsKey(QueryOptionsConstants.TYPE)) {
            return null;
        }

        Specifications speciesSpecifications = null;
        if (queryOptions.containsKey(QueryOptionsConstants.SPECIES)) {
            String[] species = queryOptions.getAsStringList(QueryOptionsConstants.SPECIES).toArray(new String[]{});
            speciesSpecifications = where(in(StudyBrowserRepository.COMMON_NAME, species)).or(in(StudyBrowserRepository.SCIENTIFIC_NAME, species));
        }

        Specifications typeSpecifications = null;
        if (queryOptions.containsKey(QueryOptionsConstants.TYPE)) {
            String[] types = queryOptions.getAsStringList(QueryOptionsConstants.TYPE).toArray(new String[]{});
            typeSpecifications = where(in(StudyBrowserRepository.EXPERIMENT_TYPE, types));
            for (String type : types) {
                typeSpecifications = typeSpecifications.or(like(StudyBrowserRepository.EXPERIMENT_TYPE, "%" + type + "%"));
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
