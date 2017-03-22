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
package uk.ac.ebi.eva.lib.repository;

import org.opencb.biodata.models.feature.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;
import uk.ac.ebi.eva.lib.filter.VariantEntityRepositoryFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of the VariantEntityRepository interface (relationship inferred by Spring),
 * due to a custom DBObject to VariantEntity conversion
 *
 * <p>It also implements the VariantEntityRepositoryCustom interface,
 * to provide an explicit implementation of the region query, using a margin for efficiency.
 */
public class VariantEntityRepositoryImpl implements VariantEntityRepositoryCustom {

    private MongoTemplate mongoTemplate;

    private final int MARGIN = 1000000;

    @Autowired
    public VariantEntityRepositoryImpl(MongoDbFactory mongoDbFactory, MappingMongoConverter mappingMongoConverter) {
        mongoTemplate = new MongoTemplate(mongoDbFactory, mappingMongoConverter);
    }

    @Override
    public List<VariantEntity> findByIdsAndComplexFilters(String id, List<VariantEntityRepositoryFilter> filters,
                                                          List<String> exclude, Pageable pageable) {
        Query query = new Query(Criteria.where("ids").is(id));
        return findByComplexFiltersHelper(query, filters, exclude, pageable);
    }

    @Override
    public Long countByIdsAndComplexFilters(String id, List<VariantEntityRepositoryFilter> filters) {
        Query query = new Query(Criteria.where("ids").is(id));
        return countByComplexFiltersHelper(query, filters);
    }

    @Override
    public List<VariantEntity> findByRegionsAndComplexFilters(List<Region> regions, List<VariantEntityRepositoryFilter> filters,
                                                              List<String> exclude, Pageable pageable) {
        Query query = new Query();
        addRegionsToQuery(query, regions);
        return findByComplexFiltersHelper(query, filters, exclude, pageable);
    }

    @Override
    public Long countByRegionsAndComplexFilters(List<Region> regions, List<VariantEntityRepositoryFilter> filters) {
        Query query = new Query();
        addRegionsToQuery(query, regions);
        return countByComplexFiltersHelper(query, filters);
    }

    @Override
    public List<String> findDistinctChromosomesByStudyId(List<String> studyIds) {
        Query query = new Query();
        query.addCriteria(Criteria.where("st.sid").in(studyIds));
        return (List<String>) mongoTemplate.getCollection(mongoTemplate.getCollectionName(VariantEntity.class))
                                           .distinct("chr", query.getQueryObject());
    }

    private List<VariantEntity> findByComplexFiltersHelper(Query query, List<VariantEntityRepositoryFilter> filters, List<String> exclude,
                                                           Pageable pageable) {

        applyFilters(query, filters);

        ArrayList<String> sortProperties = new ArrayList<String>();
        sortProperties.add("chr");
        sortProperties.add("start");
        query.with(new Sort(Sort.Direction.ASC, sortProperties));

        Pageable pageable1 = (pageable != null) ? pageable : new PageRequest(0, 10);
        query.with(pageable1);

        if (exclude != null && !exclude.isEmpty()) {
            exclude.forEach(e -> query.fields().exclude(e));
        }

        return mongoTemplate.find(query, VariantEntity.class);

    }

    private Long countByComplexFiltersHelper(Query query, List<VariantEntityRepositoryFilter> filters) {
        applyFilters(query, filters);

        return mongoTemplate.count(query, VariantEntity.class);
    }

    private void applyFilters(Query query, List<VariantEntityRepositoryFilter> filters) {
        for (VariantEntityRepositoryFilter filter : filters) {
            query.addCriteria(filter.getCriteria());
        }
    }

    private void addRegionsToQuery(Query query, List<Region> regions) {
        List<Criteria> orRegionCriteria = new ArrayList<>();

        regions.forEach(region -> orRegionCriteria.add(
                Criteria.where("chr").is(region.getChromosome())
                        .and("start").lte(region.getEnd()).gt(region.getStart() - MARGIN)
                        .and("end").gte(region.getStart()).lt(region.getEnd() + MARGIN)));

        query.addCriteria(new Criteria().orOperator(orRegionCriteria.toArray(new Criteria[orRegionCriteria.size()])));
    }

}
