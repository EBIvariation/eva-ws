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
package uk.ac.ebi.eva.lib.metadata.eva;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;

import uk.ac.ebi.eva.lib.metadata.ArchiveDBAdaptor;
import uk.ac.ebi.eva.lib.models.Assembly;
import uk.ac.ebi.eva.lib.extension.GenericSpecifications;
import uk.ac.ebi.eva.lib.utils.QueryOptions;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.lib.repositories.FileRepository;
import uk.ac.ebi.eva.lib.repositories.ProjectRepository;
import uk.ac.ebi.eva.lib.repositories.EvaStudyBrowserRepository;
import uk.ac.ebi.eva.lib.repositories.TaxonomyRepository;
import uk.ac.ebi.eva.lib.utils.QueryOptionsConstants;

import javax.persistence.Tuple;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specifications.where;

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
    public QueryResult getSpecies() {
        long start = System.currentTimeMillis();
        List<Assembly> result = taxonomyRepository.getSpecies();
        result = result.stream()
                       .filter(assembly -> assembly.getTaxonomyCode() != null && assembly.getAssemblyCode() != null)
                       .collect(Collectors.toList());
        long end = System.currentTimeMillis();
        return new QueryResult(null, ((Long) (end - start)).intValue(), result.size(), result.size(), null, null, result);
    }

    private Specification getSpeciesAndTypeFilters(QueryOptions queryOptions) {
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
                typeSpecifications = typeSpecifications.or(GenericSpecifications
                        .like(EvaStudyBrowserRepository.EXPERIMENT_TYPE, "%" + type + "%"));
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
