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

    public List<VariantEntity> findByChrAndStartWithMarginAndEndWithMargin(String chr, int start, int end) {

        Query query = new Query(
                Criteria
                        .where("chr").is(chr)
                        .and("start").lte(end).gt(start - MARGIN)
                        .and("end").gte(start).lt(end + MARGIN)
        );

        ArrayList<String> sortProps = new ArrayList<String>();
        sortProps.add("chr");
        sortProps.add("start");
        query.with(new Sort(Sort.Direction.ASC, sortProps));

        return mongoTemplate.find(query, VariantEntity.class);
    }

}
