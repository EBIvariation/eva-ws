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
    public void testQueryConsequenceType() throws Exception {
        List<String> consequenceType = new ArrayList<>();
        consequenceType.add("SO:0001234");
        variantEntityRepositoryImpl.queryConsequenceType(testQuery, consequenceType);
        expectedQuery.addCriteria(Criteria.where("annot.ct.so").in(1234));
        assertEquals(expectedQuery, testQuery);
    }

    @Test
    public void testQueryConsequenceTypeMultiple() throws Exception {
        List<String> consequenceType = new ArrayList<>();
        consequenceType.add("SO:0001234");
        consequenceType.add("SO:0001235");
        List<Integer> expectedConsequenceType = new ArrayList<>();
        expectedConsequenceType.add(1234);
        expectedConsequenceType.add(1235);
        variantEntityRepositoryImpl.queryConsequenceType(testQuery, consequenceType);
        expectedQuery.addCriteria(Criteria.where("annot.ct.so").in(expectedConsequenceType));
        assertEquals(expectedQuery, testQuery);
    }

    @Test
    public void testRelationalCriteriaHelperEquals() throws Exception {
        String jsonPath = "test.path";
        double testValue = 0.123;
        variantEntityRepositoryImpl.relationalCriteriaHelper(testQuery, jsonPath, testValue, VariantEntityRepository.RelationalOperator.EQ);
        expectedQuery.addCriteria(Criteria.where(jsonPath).is(testValue));
        assertEquals(expectedQuery, testQuery);
    }

    @Test
    public void testRelationalCriteriaHelperGreaterThan() throws Exception {
        String jsonPath = "test.path";
        double testValue = 0.123;
        variantEntityRepositoryImpl.relationalCriteriaHelper(testQuery, jsonPath, testValue, VariantEntityRepository.RelationalOperator.GT);
        expectedQuery.addCriteria(Criteria.where(jsonPath).gt(testValue));
        assertEquals(expectedQuery, testQuery);
    }

    @Test
    public void testRelationalCriteriaHelperGreaterThanEquals() throws Exception {
        String jsonPath = "test.path";
        double testValue = 0.123;
        variantEntityRepositoryImpl.relationalCriteriaHelper(testQuery, jsonPath, testValue, VariantEntityRepository.RelationalOperator.GTE);
        expectedQuery.addCriteria(Criteria.where(jsonPath).gte(testValue));
        assertEquals(expectedQuery, testQuery);
    }

    @Test
    public void testRelationalCriteriaHelperLessThanEquals() throws Exception {
        String jsonPath = "test.path";
        double testValue = 0.123;
        variantEntityRepositoryImpl.relationalCriteriaHelper(testQuery, jsonPath, testValue, VariantEntityRepository.RelationalOperator.LTE);
        expectedQuery.addCriteria(Criteria.where(jsonPath).lte(testValue));
        assertEquals(expectedQuery, testQuery);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRelationalCriteriaHelperNone() throws Exception {
        String jsonPath = "test.path";
        double testValue = 0.123;
        variantEntityRepositoryImpl.relationalCriteriaHelper(testQuery, jsonPath, testValue, VariantEntityRepository.RelationalOperator.NONE);
    }

    @Test
    public void testRelationalCriteriaHelperLessThan() throws Exception {
        String jsonPath = "test.path";
        double testValue = 0.123;
        variantEntityRepositoryImpl.relationalCriteriaHelper(testQuery, jsonPath, testValue, VariantEntityRepository.RelationalOperator.LT);
        expectedQuery.addCriteria(Criteria.where(jsonPath).lt(testValue));
        assertEquals(expectedQuery, testQuery);
    }

    @Test
    public void testQueryMafEquals() throws Exception {
        Double mafValue = 0.321;
        variantEntityRepositoryImpl.queryMaf(testQuery, mafValue, VariantEntityRepository.RelationalOperator.EQ);
        expectedQuery.addCriteria(Criteria.where("st.maf").is(mafValue));
        assertEquals(expectedQuery, testQuery);
    }

    @Test
    public void testQueryPolyphenScoreGreaterThan() throws Exception {
        Double polyphenScoreValue = 0.582;
        variantEntityRepositoryImpl.queryPolyphenScore(testQuery, polyphenScoreValue, VariantEntityRepository.RelationalOperator.GT);
        expectedQuery.addCriteria(Criteria.where("annot.ct.polyphen.sc").gt(polyphenScoreValue));
        assertEquals(expectedQuery, testQuery);
    }

    @Test
    public void testQuerySiftLessThan() throws Exception {
        Double siftValue = 0.657;
        variantEntityRepositoryImpl.querySift(testQuery, siftValue, VariantEntityRepository.RelationalOperator.LT);
        expectedQuery.addCriteria(Criteria.where("annot.ct.sift.sc").lt(siftValue));
        assertEquals(expectedQuery, testQuery);
    }

    @Test
    public void testQueryStudies() throws Exception {
        List<String> studies = new ArrayList<>();
        studies.add("PRJEB1234");
        variantEntityRepositoryImpl.queryStudies(testQuery, studies);
        expectedQuery.addCriteria(Criteria.where("files.sid").in(studies));
        assertEquals(expectedQuery, testQuery);
    }

    @Test
    public void testQueryStudiesMultiple() throws Exception {
        List<String> studies = new ArrayList<>();
        studies.add("PRJEB1234");
        studies.add("PRJEB1235");
        variantEntityRepositoryImpl.queryStudies(testQuery, studies);
        expectedQuery.addCriteria(Criteria.where("files.sid").in(studies));
        assertEquals(expectedQuery, testQuery);
    }

}