package uk.ac.ebi.eva.lib.metadata;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.utils.QueryOptionsConstants;

import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
public class StudyEvaproDBAdaptorTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudyEvaproDBAdaptor studyEvaproDBAdaptor;

    @Before
    public void setUp() throws Exception {
        EvaStudyBrowserTestData.persistTestData(entityManager);
    }

    @After
    public void tearDown() throws Exception {
        entityManager.clear();
    }

    @Test
    public void getAllStudies() throws Exception {
        QueryResult<VariantStudy> queryResult = studyEvaproDBAdaptor.getAllStudies(new QueryOptions());

        assertEquals(4, queryResult.getNumTotalResults());
    }

    @Test
    public void getAllStudiesForOneSpecies() throws Exception {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put(QueryOptionsConstants.SPECIES, EvaStudyBrowserTestData.HUMAN);
        QueryResult<VariantStudy> queryResult = studyEvaproDBAdaptor.getAllStudies(queryOptions);

        checkReturnedStudies(queryResult, 3,
                             study -> study.getSpeciesCommonName().equals(EvaStudyBrowserTestData.HUMAN));
    }

    @Test
    public void getAllStudiesForOneType() throws Exception {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put(QueryOptionsConstants.TYPE, EvaStudyBrowserTestData.EXOME_SEQUENCING);
        QueryResult<VariantStudy> queryResult = studyEvaproDBAdaptor.getAllStudies(queryOptions);

        checkReturnedStudies(queryResult, 2,
                             study -> study.getExperimentType().equals(EvaStudyBrowserTestData.EXOME_SEQUENCING));
    }

    @Test
    public void getAllStudiesForAnSpeciesAndType() throws Exception {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put(QueryOptionsConstants.SPECIES, EvaStudyBrowserTestData.HUMAN);
        queryOptions.put(QueryOptionsConstants.TYPE, EvaStudyBrowserTestData.EXOME_SEQUENCING);
        QueryResult<VariantStudy> queryResult = studyEvaproDBAdaptor.getAllStudies(queryOptions);

        checkReturnedStudies(queryResult, 1,
                             study -> study.getSpeciesCommonName().equals(EvaStudyBrowserTestData.HUMAN) && study
                                     .getExperimentType()
                                     .equals(EvaStudyBrowserTestData.EXOME_SEQUENCING));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void listStudies() throws Exception {
        studyEvaproDBAdaptor.listStudies();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void findStudyNameOrStudyId() throws Exception {
        studyEvaproDBAdaptor.findStudyNameOrStudyId("any study", new QueryOptions());
    }

    @Test
    public void getStudyById() throws Exception {
        QueryResult<VariantStudy> queryResult = studyEvaproDBAdaptor
                .getStudyById(EvaStudyBrowserTestData.PROJECT_ID_1, new QueryOptions());

        checkReturnedStudies(queryResult, 1, study -> study.getId().equals(EvaStudyBrowserTestData.PROJECT_ID_1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void close() throws Exception {
        studyEvaproDBAdaptor.close();
    }

    private void checkReturnedStudies(QueryResult<VariantStudy> queryResult, int expectedNumberOfResults,
                                      Predicate<VariantStudy> predicate) {
        assertEquals(expectedNumberOfResults, queryResult.getNumTotalResults());
        assertTrue(queryResult.getResult().stream().allMatch(predicate));
    }
}