package uk.ac.ebi.eva.lib.metadata;

import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.adaptors.ArchiveDBAdaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;
import uk.ac.ebi.eva.lib.models.Assembly;
import uk.ac.ebi.eva.lib.extension.GenericSpecifications;
import uk.ac.ebi.eva.lib.repository.FileRepository;
import uk.ac.ebi.eva.lib.repository.ProjectRepository;
import uk.ac.ebi.eva.lib.repository.EvaStudyBrowserRepository;
import uk.ac.ebi.eva.lib.repository.TaxonomyRepository;
import uk.ac.ebi.eva.lib.utils.QueryOptionsConstants;

import javax.persistence.Tuple;
import java.util.*;

import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * Created by jorizci on 03/10/16.
 */
@Component
public class ArchiveEvaproDBAdaptor implements ArchiveDBAdaptor {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EvaStudyBrowserRepository evaStudyBrowserRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private TaxonomyRepository taxonomyRepository;

    @Override
    public QueryResult countStudies() {
        long start = System.currentTimeMillis();
        long count = projectRepository.count();
        long end = System.currentTimeMillis();
        return new QueryResult(null, ((Long) (end - start)).intValue(), 1, 1, null, null, Arrays.asList(count));
    }

    @Override
    public QueryResult countStudiesPerSpecies(QueryOptions queryOptions) {
        long start = System.currentTimeMillis();
        Specification filterSpecification = getSpeciesAndTypeFilters(queryOptions);
        List<Tuple> countGroupBy = evaStudyBrowserRepository.groupCount(EvaStudyBrowserRepository.COMMON_NAME, filterSpecification, false);
        List<Map.Entry<String, Long>> result = new ArrayList<>();
        for (Tuple tuple : countGroupBy) {
            String species = tuple.get(0) != null ? (String) tuple.get(0) : "Others";
            long count = (long) tuple.get(1);
            result.add(new AbstractMap.SimpleEntry<>(species, count));
        }
        long end = System.currentTimeMillis();
        return new QueryResult(null, ((Long) (end - start)).intValue(), result.size(), result.size(), null, null, result);
    }

    @Override
    public QueryResult countStudiesPerType(QueryOptions queryOptions) {
        long start = System.currentTimeMillis();
        Specification filterSpecification = getSpeciesAndTypeFilters(queryOptions);
        List<Tuple> countGroupBy = evaStudyBrowserRepository.groupCount(EvaStudyBrowserRepository.EXPERIMENT_TYPE, filterSpecification, false);
        List<Map.Entry<String, Long>> result = new ArrayList<>();
        for (Tuple tuple : countGroupBy) {
            String species = tuple.get(0) != null ? (String) tuple.get(0) : "Others";
            long count = (long) tuple.get(1);
            result.add(new AbstractMap.SimpleEntry<>(species, count));
        }
        long end = System.currentTimeMillis();
        return new QueryResult(null, ((Long) (end - start)).intValue(), result.size(), result.size(), null, null, result);
    }

    @Override
    public QueryResult countFiles() {
        long start = System.currentTimeMillis();
        long count = fileRepository.countByFileTypeIn(Arrays.asList("vcf", "vcf_aggregate"));
        long end = System.currentTimeMillis();
        return new QueryResult(null, ((Long) (end - start)).intValue(), 1, 1, null, null, Arrays.asList(count));
    }

    @Override
    public QueryResult countSpecies() {
        long start = System.currentTimeMillis();
        long count = taxonomyRepository.count();
        long end = System.currentTimeMillis();
        return new QueryResult(null, ((Long) (end - start)).intValue(), 1, 1, null, null, Arrays.asList(count));
    }

    @Override
    public QueryResult getSpecies(String s, boolean b) {
        long start = System.currentTimeMillis();
        List<Assembly> result = taxonomyRepository.getSpecies();
        long end = System.currentTimeMillis();
        return new QueryResult(null, ((Long) (end - start)).intValue(), result.size(), result.size(), null, null, result);
    }

    private Specification getSpeciesAndTypeFilters(QueryOptions queryOptions) {
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
