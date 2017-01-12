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
import uk.ac.ebi.eva.lib.repository.VariantEntityRepository.RelationalOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public List<VariantEntity> findByIdsAndComplexFilters(String id, List<String> studies, List<String> consequenceType,
                                                          RelationalOperator mafOperator,
                                                          Double mafValue,
                                                          RelationalOperator polyphenScoreOperator,
                                                          Double polyphenScoreValue,
                                                          RelationalOperator siftScoreOperator,
                                                          Double siftScoreValue,
                                                          List<String> exclude, Pageable pageable) {
        Query query = new Query(Criteria.where("ids").is(id));
        return findByComplexFiltersHelper(query, studies, consequenceType, mafOperator, mafValue,
                                          polyphenScoreOperator, polyphenScoreValue,
                                          siftScoreOperator, siftScoreValue, exclude, pageable);
    }

    @Override
    public Long countByIdsAndComplexFilters(String id, List<String> studies, List<String> consequenceType,
                                            RelationalOperator mafOperator, Double mafValue,
                                            RelationalOperator polyphenScoreOperator, Double polyphenScoreValue,
                                            RelationalOperator siftScoreOperator, Double siftScoreValue) {
        Query query = new Query(Criteria.where("ids").is(id));
        return countByComplexFiltersHelper(query, studies, consequenceType, mafOperator, mafValue,
                                           polyphenScoreOperator, polyphenScoreValue, siftScoreOperator,
                                           siftScoreValue);
    }

    @Override
    public List<VariantEntity> findByRegionsAndComplexFilters(List<Region> regions, List<String> studies,
                                                              List<String> consequenceType,
                                                              RelationalOperator mafOperator,
                                                              Double mafValue,
                                                              RelationalOperator polyphenScoreOperator,
                                                              Double polyphenScoreValue,
                                                              RelationalOperator siftScoreOperator,
                                                              Double siftScoreValue,
                                                              List<String> exclude, Pageable pageable) {
        Query query = new Query();
        addRegionsToQuery(query, regions);
        return findByComplexFiltersHelper(query, studies, consequenceType, mafOperator, mafValue,
                                          polyphenScoreOperator, polyphenScoreValue,
                                          siftScoreOperator, siftScoreValue, exclude, pageable);
    }

    @Override
    public Long countByRegionsAndComplexFilters(List<Region> regions, List<String> studies,
                                                List<String> consequenceType,
                                                RelationalOperator mafOperator, Double mafValue,
                                                RelationalOperator polyphenScoreOperator, Double polyphenScoreValue,
                                                RelationalOperator siftScoreOperator, Double siftScoreValue) {
        Query query = new Query();
        addRegionsToQuery(query, regions);
        return countByComplexFiltersHelper(query, studies, consequenceType, mafOperator, mafValue,
                                           polyphenScoreOperator, polyphenScoreValue, siftScoreOperator,
                                           siftScoreValue);
    }

    List<VariantEntity> findByComplexFiltersHelper(Query query,
                                                   List<String> studies, List<String> consequenceType,
                                                   RelationalOperator mafOperator,
                                                   Double mafValue,
                                                   RelationalOperator polyphenScoreOperator,
                                                   Double polyphenScoreValue,
                                                   RelationalOperator siftScoreOperator,
                                                   Double siftScoreValue,
                                                   List<String> exclude, Pageable pageable) {

        applyFilters(query, studies, consequenceType, mafOperator, mafValue,
                     polyphenScoreOperator, polyphenScoreValue,
                     siftScoreOperator, siftScoreValue);

        ArrayList<String> sortProps = new ArrayList<String>();
        sortProps.add("chr");
        sortProps.add("start");
        query.with(new Sort(Sort.Direction.ASC, sortProps));

        Pageable pageable1 = (pageable != null) ? pageable : new PageRequest(0, 10);
        query.with(pageable1);

        if (Objects.nonNull(exclude) && !exclude.isEmpty()) {
            exclude.forEach(e -> query.fields().exclude(e));
        }

        return mongoTemplate.find(query, VariantEntity.class);

    }

    Long countByComplexFiltersHelper(Query query,
                                     List<String> studies, List<String> consequenceType,
                                     RelationalOperator mafOperator,
                                     Double mafValue,
                                     RelationalOperator polyphenScoreOperator,
                                     Double polyphenScoreValue,
                                     RelationalOperator siftScoreOperator,
                                     Double siftScoreValue) {
        applyFilters(query, studies, consequenceType, mafOperator, mafValue,
                     polyphenScoreOperator, polyphenScoreValue,
                     siftScoreOperator, siftScoreValue);

        return mongoTemplate.count(query, VariantEntity.class);
    }

    void applyFilters(Query query,
                       List<String> studies, List<String> consequenceType,
                       RelationalOperator mafOperator,
                       Double mafValue,
                       RelationalOperator polyphenScoreOperator,
                       Double polyphenScoreValue,
                       RelationalOperator siftScoreOperator,
                       Double siftScoreValue) {

        if (consequenceType != null && !consequenceType.isEmpty()) {
            queryConsequenceType(query, consequenceType);
        }

        if (mafValue != null && mafOperator != VariantEntityRepository.RelationalOperator.NONE) {
            queryMaf(query, mafValue, mafOperator);
        }

        if (polyphenScoreValue != null && polyphenScoreOperator != VariantEntityRepository.RelationalOperator.NONE) {
            queryPolyphenScore(query, polyphenScoreValue, polyphenScoreOperator);
        }

        if (siftScoreValue != null && siftScoreOperator != VariantEntityRepository.RelationalOperator.NONE) {
            querySift(query, siftScoreValue, siftScoreOperator);
        }

        if (studies != null && !studies.isEmpty()) {
            queryStudies(query, studies);
        }
    }

    void addRegionsToQuery(Query query, List<Region> regions) {
        List<Criteria> orRegionCriteria = new ArrayList<>();

        regions.forEach(region -> orRegionCriteria.add(
                Criteria.where("chr").is(region.getChromosome())
                        .and("start").lte(region.getEnd()).gt(region.getStart() - MARGIN)
                        .and("end").gte(region.getStart()).lt(region.getEnd() + MARGIN)));

        query.addCriteria(new Criteria().orOperator(orRegionCriteria.toArray(new Criteria[orRegionCriteria.size()])));
    }

    void queryConsequenceType(Query query, List<String> consequenceType) {
        List<Integer> consequenceTypeConv = consequenceType.stream()
                                                           .map(c -> Integer.parseInt(c.replaceAll("[^\\d.]", ""), 10))
                                                           .collect(Collectors.toList());
        query.addCriteria(Criteria.where("annot.ct.so").in(consequenceTypeConv));
    }

    void queryMaf(Query query, double mafValue, RelationalOperator mafOperator) {
        relationalCriteriaHelper(query, "st.maf", mafValue, mafOperator);
    }

    void queryPolyphenScore(Query query, double polyphenScoreValue, RelationalOperator polyphenScoreOperator) {
        relationalCriteriaHelper(query, "annot.ct.polyphen.sc", polyphenScoreValue, polyphenScoreOperator);
    }

    void querySift(Query query, double siftScoreValue, RelationalOperator siftScoreOperator) {
        relationalCriteriaHelper(query, "annot.ct.sift.sc", siftScoreValue, siftScoreOperator);
    }

    void queryStudies(Query query, List<String> studies) {
        query.addCriteria(Criteria.where("files.sid").in(studies));
    }

    void relationalCriteriaHelper(Query query, String field, double value, RelationalOperator operator) {

        Criteria criteria = Criteria.where(field);
        switch (operator) {
            case EQ:
                criteria = criteria.is(value);
                break;
            case GT:
                criteria = criteria.gt(value);
                break;
            case LT:
                criteria = criteria.lt(value);
                break;
            case GTE:
                criteria = criteria.gte(value);
                break;
            case LTE:
                criteria = criteria.lte(value);
                break;
            case NONE:
                throw new IllegalArgumentException();
        }
        query.addCriteria(criteria);
    }
}
