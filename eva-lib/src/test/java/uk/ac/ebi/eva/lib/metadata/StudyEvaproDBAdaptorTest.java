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

import uk.ac.ebi.eva.lib.entity.EvaStudyBrowser;
import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.utils.QueryOptionsConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
public class StudyEvaproDBAdaptorTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudyEvaproDBAdaptor studyEvaproDBAdaptor;

    private static final String HUMAN = "human";

    private static final String EXOME_SEQUENCING = "Exome sequencing";

    private static final String PROJECT_ID_1 = "PRJ0001";

    @Before
    public void setUp() throws Exception {
        EvaStudyBrowser humanWGSStudy1 = new EvaStudyBrowser(PROJECT_ID_1, 1, "Project 1", "This is project 1", "1111",
                                                             HUMAN, "Homo sapiens", "source 1",
                                                             "Whole genome sequencing", 1000000L, 1000, "center 1",
                                                             "scope 1", "DNA", "PubMed:1111", "PRJ00002",
                                                             "whole genome seq", "WGS", "GCA_000001405.22", "GRCh38.p7",
                                                             "Illumina", "Resource1");
        EvaStudyBrowser humanWGSStudy2 = new EvaStudyBrowser("PRJ0002", 2, "Project 2", "This is project 2", "2222",
                                                             HUMAN, "Homo sapiens", "source 2",
                                                             "Whole genome sequencing", 2000000L, 2000,
                                                             "center 2", "scope 2", "DNA", "PubMed:2222", "PRJ00002",
                                                             "whole genome seq", "WGS", "GCA_000001405.22", "GRCh38.p7",
                                                             "Illumina", "Resource2");
        EvaStudyBrowser humanESStudy = new EvaStudyBrowser("PRJ0003", 3, "Project 3", "This is project 3", "3333",
                                                           HUMAN, "Homo sapiens", "source 3", EXOME_SEQUENCING,
                                                           3000000L, 3000,
                                                           "center 3", "scope 3", "DNA", "PubMed:3333", "PRJ00003",
                                                           EXOME_SEQUENCING, "ES", "GCA_000001405.14", "GRCh37.p13",
                                                           "Illumina", "Resource3");
        EvaStudyBrowser cowESStudy = new EvaStudyBrowser("PRJ0004", 4, "Project 4", "This is project 4", "4444",
                                                         "cow", "Bos taurus", "source 4", EXOME_SEQUENCING, 4000000L,
                                                         4000,
                                                         "center 4", "scope 4", "DNA", "PubMed:4444", "PRJ00004",
                                                         EXOME_SEQUENCING, "ES", "GCA_000003055.3", "UMD3.1",
                                                         "Illumina", "Resource4");
        entityManager.persist(humanWGSStudy1);
        entityManager.persist(humanWGSStudy2);
        entityManager.persist(humanESStudy);
        entityManager.persist(cowESStudy);

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getAllStudies() throws Exception {
        QueryResult<VariantStudy> queryResult = studyEvaproDBAdaptor.getAllStudies(new QueryOptions());

        assertEquals(4, queryResult.getNumTotalResults());

    }

    @Test
    public void getAllStudiesForOneSpecies() throws Exception {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put(QueryOptionsConstants.SPECIES, HUMAN);
        QueryResult<VariantStudy> queryResult = studyEvaproDBAdaptor.getAllStudies(queryOptions);

        assertEquals(3, queryResult.getNumTotalResults());
        assertTrue(queryResult.getResult().stream().allMatch(study -> study.getSpeciesCommonName().equals(HUMAN)));
    }

    @Test
    public void getAllStudiesForOneType() throws Exception {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put(QueryOptionsConstants.TYPE, EXOME_SEQUENCING);
        QueryResult<VariantStudy> queryResult = studyEvaproDBAdaptor.getAllStudies(queryOptions);

        assertEquals(2, queryResult.getNumTotalResults());
        assertTrue(
                queryResult.getResult().stream().allMatch(study -> study.getExperimentType().equals(EXOME_SEQUENCING)));
    }

    @Test
    public void getAllStudiesForAnSpeciesAndType() throws Exception {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put(QueryOptionsConstants.SPECIES, HUMAN);
        queryOptions.put(QueryOptionsConstants.TYPE, EXOME_SEQUENCING);
        QueryResult<VariantStudy> queryResult = studyEvaproDBAdaptor.getAllStudies(queryOptions);

        assertEquals(1, queryResult.getNumTotalResults());
        assertTrue(queryResult.getResult().stream().allMatch(
                study -> study.getSpeciesCommonName().equals(HUMAN) && study.getExperimentType()
                                                                            .equals(EXOME_SEQUENCING)));
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
        QueryResult<VariantStudy> queryResult = studyEvaproDBAdaptor.getStudyById(PROJECT_ID_1, new QueryOptions());

        assertEquals(1, queryResult.getNumTotalResults());
        assertTrue(queryResult.getResult().stream().allMatch(study -> study.getId().equals(PROJECT_ID_1)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void close() throws Exception {
        studyEvaproDBAdaptor.close();
    }

}