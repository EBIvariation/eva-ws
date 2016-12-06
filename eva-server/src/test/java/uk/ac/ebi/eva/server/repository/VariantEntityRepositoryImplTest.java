package uk.ac.ebi.eva.server.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories
@ComponentScan("uk.ac.ebi.eva.server.repository")
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

    @Test
    public void queryConsequenceType() throws Exception {
        List<String> consequenceType = new ArrayList<>();
        consequenceType.add("SO:0001234");
        variantEntityRepositoryImpl.queryConsequenceType(testQuery, consequenceType);
        expectedQuery.addCriteria(Criteria.where("annot.ct.so").in(1234));
        assertEquals(testQuery, expectedQuery);
    }

    @Test
    public void relationalCriteriaHelper() throws Exception {
        for (VariantEntityRepository.RelationalOperator value : VariantEntityRepository.RelationalOperator.values()) {

        }
    }

    @Test
    public void queryMaf() throws Exception {
        Double mafValue = 0.321;
        variantEntityRepositoryImpl.queryMaf(testQuery, mafValue, VariantEntityRepository.RelationalOperator.EQ);
        expectedQuery.addCriteria(Criteria.where("st.maf").is(mafValue));
        assertEquals(testQuery, expectedQuery);
    }

    @Test
    public void queryPolyphenScore() throws Exception {
        Double polyphenScoreValue = 0.582;
        variantEntityRepositoryImpl.queryPolyphenScore(testQuery, polyphenScoreValue, VariantEntityRepository.RelationalOperator.GT);
        expectedQuery.addCriteria(Criteria.where("annot.ct.polyphen.sc").gt(polyphenScoreValue));
        assertEquals(testQuery, expectedQuery);
    }

    @Test
    public void querySift() throws Exception {
        Double siftValue = 0.657;
        variantEntityRepositoryImpl.querySift(testQuery, siftValue, VariantEntityRepository.RelationalOperator.LT);
        expectedQuery.addCriteria(Criteria.where("annot.ct.sift.sc").lt(siftValue));
        assertEquals(testQuery, expectedQuery);
    }

    @Test
    public void queryStudies() throws Exception {
        List<String> studies = new ArrayList<>();
        studies.add("PRJEB1234");
        variantEntityRepositoryImpl.queryStudies(testQuery, studies);
        expectedQuery.addCriteria(Criteria.where("files.sid").in(studies));
        assertEquals(testQuery, expectedQuery);
    }

}