package uk.ac.ebi.eva.lib.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.eva.lib.configuration.MongoRepositoryTestConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoRepositoryTestConfiguration.class})
public class VariantEntityRepositoryImplTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MongoDbFactory mongoDbFactory;

    @Autowired
    private MappingMongoConverter mappingMongoConverter;

    private VariantEntityRepositoryImpl variantEntityRepositoryImpl;

    private Query testQuery;
    private Query expectedQuery;

    @Before
    public void setUp() {
        variantEntityRepositoryImpl = new VariantEntityRepositoryImpl(mongoDbFactory, mappingMongoConverter);
        testQuery = new Query(Criteria
                                   .where("chr").is("1")
                                   .and("start").lte(1).gt(1 - 1000000)
                                   .and("end").gte(1).lt(1 + 1000000)
        );
        expectedQuery = new Query(Criteria
                                   .where("chr").is("1")
                                   .and("start").lte(1).gt(1 - 1000000)
                                   .and("end").gte(1).lt(1 + 1000000)
        );
    }

}