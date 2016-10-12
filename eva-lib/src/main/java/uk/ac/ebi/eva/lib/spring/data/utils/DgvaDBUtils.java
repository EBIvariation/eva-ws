package uk.ac.ebi.eva.lib.spring.data.utils;

import org.opencb.datastore.core.QueryOptions;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import uk.ac.ebi.eva.lib.spring.data.repository.DGvaStudyBrowserRepository;

import static org.springframework.data.jpa.domain.Specifications.where;
import static uk.ac.ebi.eva.lib.spring.data.extension.GenericSpecifications.in;
import static uk.ac.ebi.eva.lib.spring.data.extension.GenericSpecifications.like;

/**
 * Created by jorizci on 03/10/16.
 */
public class DgvaDBUtils {

    public static Specification getSpeciesAndTypeFilters(QueryOptions queryOptions) {
        if (!queryOptions.containsKey(QueryOptionsConstants.SPECIES) && !queryOptions.containsKey(QueryOptionsConstants.TYPE)) {
            return null;
        }

        Specifications speciesSpecifications = null;
        if (queryOptions.containsKey(QueryOptionsConstants.SPECIES)) {
            String[] species = queryOptions.getAsStringList(QueryOptionsConstants.SPECIES).toArray(new String[]{});
            speciesSpecifications = where(in(DGvaStudyBrowserRepository.COMMON_NAME, species)).or(in(DGvaStudyBrowserRepository.SCIENTIFIC_NAME, species));
        }

        Specifications typeSpecifications = null;
        if (queryOptions.containsKey(QueryOptionsConstants.TYPE)) {
            String[] types = queryOptions.getAsStringList(QueryOptionsConstants.TYPE).toArray(new String[]{});
            typeSpecifications = where(in(DGvaStudyBrowserRepository.STUDY_TYPE, types));
            for (String type : types) {
                typeSpecifications = typeSpecifications.or(like(DGvaStudyBrowserRepository.STUDY_TYPE, "%" + type + "%"));
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
