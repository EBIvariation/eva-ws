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

import uk.ac.ebi.eva.lib.entity.DgvaStudyBrowser;
import uk.ac.ebi.eva.lib.utils.QueryOptionsConstants;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ArchiveDgvaDBAdaptorTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ArchiveDgvaDBAdaptor archiveDgvaDBAdaptor;

    private static final String HUMAN = "Human";

    private static final String MOUSE = "Mouse";

    private static final String CONTROL_SET = "Control Set";

    public static final String COLLECTION = "Collection";

    @Before
    public void setUp() throws Exception {
        DgvaStudyBrowser study1 = new DgvaStudyBrowser("estd1", 1000, 10, 100, "9606", HUMAN, "Homo sapiens", "1111",
                                                       "alias1", "Study 1", CONTROL_SET, "PRJ1", "www.study1.com",
                                                       "This is study 1", "Sequence alignment", "HMM", "Sequencing",
                                                       "Illumina", "GRCh38");
        DgvaStudyBrowser study2= new DgvaStudyBrowser("estd2", 2000, 20, 200, "9606", HUMAN, "Homo sapiens", "2222",
                                                      "alias2", "Study 2", COLLECTION, "PRJ2", "www.study2.com",
                                                      "This is study 2", "Sequence alignment", "HMM", "Sequencing",
                                                      "Illumina", "GRCh38");
        DgvaStudyBrowser study3 = new DgvaStudyBrowser("estd3", 3000, 30, 300, "9606", MOUSE, "MMusculus", "3333",
                                                       "alias3", "Study 3", CONTROL_SET, "PRJ3", "www.study3.com",
                                                       "This is study 3", "Sequence alignment", "HMM", "Sequencing",
                                                       "Illumina", "MGSCv37");

        entityManager.persist(study1);
        entityManager.persist(study2);
        entityManager.persist(study3);
    }

    @Test
    public void countStudies() throws Exception {
        QueryResult<Long> queryResult = archiveDgvaDBAdaptor.countStudies();

        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(3, queryResult.getResult().get(0).longValue());
    }

    @Test
    public void countStudiesPerSpeciesFilteringBySpecies() throws Exception {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerSpecies(new QueryOptions(QueryOptionsConstants.SPECIES, HUMAN));

        assertEquals(1, queryResult.getNumTotalResults());
        Map.Entry<String, Long> result = queryResult.getResult().get(0);
        assertEquals(HUMAN, result.getKey());
        assertEquals(2, result.getValue().longValue());
    }

    @Test
    public void countStudiesPerSpeciesFilteringBySpeciesAndType() throws Exception {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put(QueryOptionsConstants.SPECIES, HUMAN);
        queryOptions.put(QueryOptionsConstants.TYPE, CONTROL_SET);
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor.countStudiesPerSpecies(queryOptions);

        assertEquals(1, queryResult.getNumTotalResults());
        Map.Entry<String, Long> result = queryResult.getResult().get(0);
        assertEquals(HUMAN, result.getKey());
        assertEquals(1, result.getValue().longValue());
    }

    @Test
    public void countStudiesPerSpeciesUnfiltered() throws Exception {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerSpecies(new QueryOptions());

        assertEquals(2, queryResult.getNumTotalResults());
        List<Map.Entry<String, Long>> results =  queryResult.getResult();
        long humanStudiesCount = results.stream().filter(e -> e.getKey().equals(HUMAN))
                                        .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        long mouseStudiesCount = results.stream().filter(e -> e.getKey().equals(MOUSE))
                                      .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        assertEquals(2, humanStudiesCount);
        assertEquals(1, mouseStudiesCount);
    }

    @Test
    public void countStudiesPerTypeFilteringByType() throws Exception {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerType(new QueryOptions(QueryOptionsConstants.TYPE, CONTROL_SET));

        assertEquals(1, queryResult.getNumTotalResults());
        Map.Entry<String, Long> result = queryResult.getResult().get(0);
        assertEquals(CONTROL_SET, result.getKey());
        assertEquals(2, result.getValue().longValue());
    }

    @Test
    public void countStudiesPerTypeUnfiltered() throws Exception {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerType(new QueryOptions());

        assertEquals(2, queryResult.getNumTotalResults());
        List<Map.Entry<String, Long>> results =  queryResult.getResult();
        long controlSetStudiesCount = results.stream().filter(e -> e.getKey().equals(CONTROL_SET))
                                        .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        long collectionStudiesCount = results.stream().filter(e -> e.getKey().equals(COLLECTION))
                                        .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        assertEquals(2, controlSetStudiesCount);
        assertEquals(1, collectionStudiesCount);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void countFiles() throws Exception {
        archiveDgvaDBAdaptor.countFiles();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void countSpecies() throws Exception {
        archiveDgvaDBAdaptor.countSpecies();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSpecies() throws Exception {
        archiveDgvaDBAdaptor.getSpecies(HUMAN, true);
    }
}