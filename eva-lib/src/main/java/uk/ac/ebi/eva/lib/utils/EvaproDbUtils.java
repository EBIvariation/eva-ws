package uk.ac.ebi.eva.lib.utils;

import org.opencb.datastore.core.QueryOptions;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import uk.ac.ebi.eva.lib.extension.GenericSpecifications;
import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.repository.EvaStudyBrowserRepository;

import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * Created by jorizci on 04/10/16.
 */
public class EvaproDbUtils {

    public static VariantStudy.StudyType stringToStudyType(String studyType) {
        switch (studyType) {
            case "Collection":
            case "Curated Collection":
                return VariantStudy.StudyType.COLLECTION;
            case "Control Set":
            case "Control-Set":
                return VariantStudy.StudyType.CONTROL;
            case "Case Control":
            case "Case-Control":
                return VariantStudy.StudyType.CASE_CONTROL;
            case "Case Set":
            case "Case-Set":
                return VariantStudy.StudyType.CASE;
            case "Tumor vs. Matched-Normal":
                return VariantStudy.StudyType.PAIRED_TUMOR;
            case "Aggregate":
                return VariantStudy.StudyType.AGGREGATE;
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
            String[] species = queryOptions.getAsStringList(QueryOptionsConstants.SPECIES).toArray(new String[]{});
            speciesSpecifications = where(GenericSpecifications.in(EvaStudyBrowserRepository.COMMON_NAME, species)).or(GenericSpecifications.in(EvaStudyBrowserRepository.SCIENTIFIC_NAME, species));
        }

        Specifications typeSpecifications = null;
        if (queryOptions.containsKey(QueryOptionsConstants.TYPE)) {
            String[] types = queryOptions.getAsStringList(QueryOptionsConstants.TYPE).toArray(new String[]{});
            typeSpecifications = where(GenericSpecifications.in(EvaStudyBrowserRepository.EXPERIMENT_TYPE, types));
            for (String type : types) {
                typeSpecifications = typeSpecifications.or(GenericSpecifications.like(EvaStudyBrowserRepository.EXPERIMENT_TYPE, "%" + type + "%"));
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
