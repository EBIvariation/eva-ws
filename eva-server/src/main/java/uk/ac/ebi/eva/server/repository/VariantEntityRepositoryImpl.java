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
package uk.ac.ebi.eva.server.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete implementation of the VariantEntityRepository interface (relationship inferred by Spring),
 * due to a custom DBObject to VariantEntity conversion
 *
 * <p>It also implements the VariantEntityRepositoryCustom interface,
 * to provide an explicit implementation of the region query, using a margin for efficiency.
 */
public class VariantEntityRepositoryImpl implements VariantEntityRepositoryCustom {

    private MongoDbFactory mongoDbFactory;
    private MongoTemplate mongoTemplate;
    private MappingMongoConverter mappingMongoConverter;

    private final int MARGIN = 1000000;

    @Autowired
    public VariantEntityRepositoryImpl(MongoDbFactory mongoDbFactory, MappingMongoConverter mappingMongoConverter) {
        this.mongoDbFactory = mongoDbFactory;
        this.mappingMongoConverter = mappingMongoConverter;
        mongoTemplate = new MongoTemplate(mongoDbFactory, mappingMongoConverter);
    }



    void queryConsequenceType(Query query, List<String> consequenceType) {
        List<Integer> consequenceTypeConv = consequenceType.stream()
                                                           .map(c -> Integer.parseInt(c.replaceAll("[^\\d.]", ""), 10))
                                                           .collect(Collectors.toList());
        query.addCriteria(Criteria.where("annot.ct.so").in(consequenceTypeConv));
    }

    void relationalCriteriaHelper(Query query, String jsonPath, Double value,
                                  VariantEntityRepository.RelationalOperator operator) {

        Criteria criteria = Criteria.where(jsonPath);
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

    void queryMaf(Query query, Double mafValue, VariantEntityRepository.RelationalOperator mafOperator) {
        relationalCriteriaHelper(query, "st.maf", mafValue, mafOperator);
    }

    void queryPolyphenScore(Query query, Double polyphenScoreValue,
                            VariantEntityRepository.RelationalOperator polyphenScoreOperator) {
        relationalCriteriaHelper(query, "annot.ct.polyphen.sc", polyphenScoreValue, polyphenScoreOperator);
    }

    void querySift(Query query, Double siftValue, VariantEntityRepository.RelationalOperator siftOperator) {
        relationalCriteriaHelper(query, "annot.ct.sift.sc", siftValue, siftOperator);
    }

    void queryStudies(Query query, List<String> studies) {
        query.addCriteria(Criteria.where("files.sid").in(studies));
    }

    @Override
    public List<VariantEntity> findByRegionAndComplexFilters(String chr, int start, int end,
                                                             List<String> consequenceType,
                                                             VariantEntityRepository.RelationalOperator mafOperator,
                                                             Double mafValue,
                                                             VariantEntityRepository.RelationalOperator polyphenScoreOperator,
                                                             Double polyphenScoreValue,
                                                             VariantEntityRepository.RelationalOperator siftOperator,
                                                             Double siftValue,
                                                             List<String> studies) {
        Query query = new Query(Criteria
                                        .where("chr").is(chr)
                                        .and("start").lte(end).gt(start - MARGIN)
                                        .and("end").gte(start).lt(end + MARGIN)
        );

        if (consequenceType != null && !consequenceType.isEmpty()) {
            queryConsequenceType(query, consequenceType);
        }

        if (mafValue != null && mafOperator != null) {
            queryMaf(query, mafValue, mafOperator);
        }

        if (polyphenScoreValue != null && polyphenScoreOperator != null) {
            queryPolyphenScore(query, polyphenScoreValue, polyphenScoreOperator);
        }

        if (siftValue != null && siftOperator != null) {
            querySift(query, siftValue, siftOperator);
        }

        if (studies != null && !studies.isEmpty()) {
            queryStudies(query, studies);
        }

        ArrayList<String> sortProps = new ArrayList<String>();
        sortProps.add("chr");
        sortProps.add("start");
        query.with(new Sort(Sort.Direction.ASC, sortProps));

        return mongoTemplate.find(query, VariantEntity.class);
    }
}
