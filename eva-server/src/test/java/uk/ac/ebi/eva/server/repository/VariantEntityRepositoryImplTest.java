package uk.ac.ebi.eva.server.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@ComponentScan(basePackageClasses = { VariantEntityRepositoryImpl.class })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class VariantEntityRepositoryImplTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private VariantEntityRepositoryImpl variantEntityRepositoryImpl;

    private Query queryA;
    private Query queryB;

    @Before
    public void setUp() {
        queryA = new Query(Criteria
                                   .where("chr").is("1")
                                   .and("start").lte(1).gt(1 - 1000000)
                                   .and("end").gte(1).lt(1 + 1000000)
        );
        queryB = new Query(Criteria
                                   .where("chr").is("1")
                                   .and("start").lte(1).gt(1 - 1000000)
                                   .and("end").gte(1).lt(1 + 1000000)
        );
    }

    @Test
    public void queryConsequenceType() throws Exception {
        List<String> consequenceType = new ArrayList<>();
        consequenceType.add("SO:0001234");
        variantEntityRepositoryImpl.queryConsequenceType(queryA, consequenceType);
        queryB.addCriteria(Criteria.where("annot.ct.so").in("1234"));
        assertEquals(queryA, queryB);
    }

    @Test
    public void relationalCriteriaHelper() throws Exception {

    }

    @Test
    public void queryMaf() throws Exception {

    }

    @Test
    public void queryPolyphenScore() throws Exception {

    }

    @Test
    public void querySift() throws Exception {

    }

    @Test
    public void queryStudies() throws Exception {

    }

}