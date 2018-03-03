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
package uk.ac.ebi.eva.lib.metadata.dgva;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import uk.ac.ebi.eva.lib.models.VariantStudy;

import uk.ac.ebi.eva.lib.metadata.ArchiveDBAdaptor;
import uk.ac.ebi.eva.lib.utils.QueryOptions;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.lib.repositories.DgvaStudyBrowserRepository;

import javax.persistence.Tuple;
import java.util.*;

import static uk.ac.ebi.eva.lib.dgva_utils.DgvaDBUtils.getSpeciesAndTypeFilters;

@Component
public class ArchiveDgvaDBAdaptor implements ArchiveDBAdaptor {

    @Autowired
    private DgvaStudyBrowserRepository dgvaStudyBrowserRepository;

    @Override
    public QueryResult countStudies() {
        long start = System.currentTimeMillis();
        long count = dgvaStudyBrowserRepository.count();
        long end = System.currentTimeMillis();
        return new QueryResult(null, ((Long) (end - start)).intValue(), 1, 1, null, null, Arrays.asList(count));
    }

    @Override
    public QueryResult countStudiesPerSpecies(QueryOptions queryOptions) {
        long start = System.currentTimeMillis();
        Specification filterSpecification = getSpeciesAndTypeFilters(queryOptions);
        List<Tuple> countGroupBy = dgvaStudyBrowserRepository.groupCount(DgvaStudyBrowserRepository.COMMON_NAME, filterSpecification, false);
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
        List<Tuple> countGroupBy = dgvaStudyBrowserRepository.groupCount(DgvaStudyBrowserRepository.STUDY_TYPE, filterSpecification, false);
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult countSpecies() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult countVariants(List<VariantStudy> variantStudies) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult getSpecies() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
