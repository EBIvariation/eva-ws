package uk.ac.ebi.eva.lib.metadata;

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

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class StudyDgvaDBAdaptorTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudyDgvaDBAdaptor studyDgvaDBAdaptor;

    @Before
    public void setUp() throws Exception {
        DgvaStudyTestData.persistTestData(entityManager);
    }

    @Test
    public void getAllStudiesUnfiltered() throws Exception {
        QueryResult<VariantStudy> queryResult = studyDgvaDBAdaptor.getAllStudies(new QueryOptions());

        assertEquals(3, queryResult.getNumTotalResults());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void listStudies() throws Exception {
        studyDgvaDBAdaptor.listStudies();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void findStudyNameOrStudyId() throws Exception {
        studyDgvaDBAdaptor.findStudyNameOrStudyId("Study", new QueryOptions());
    }

    @Test
    public void getStudyById() throws Exception {
        QueryResult<VariantStudy> queryResult = studyDgvaDBAdaptor.getStudyById(DgvaStudyTestData.STUDY_1_ID, new QueryOptions());

        assertEquals(1, queryResult.getNumTotalResults());
        VariantStudy variantStudy = queryResult.first();
        assertEquals(DgvaStudyTestData.STUDY_1_ID, variantStudy.getId());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void close() throws Exception {
        studyDgvaDBAdaptor.close();
    }

}