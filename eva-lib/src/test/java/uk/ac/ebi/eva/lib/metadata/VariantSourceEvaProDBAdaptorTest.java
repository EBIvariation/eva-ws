package uk.ac.ebi.eva.lib.metadata;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencb.biodata.models.variant.stats.VariantSourceStats;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
public class VariantSourceEvaProDBAdaptorTest {


    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VariantSourceEvaProDBAdaptor variantSourceEvaproDBAdaptor;

    @Before
    public void setUp() throws Exception {
        FileTestData.persistTestData(entityManager);
    }

    @Test
    public void countSources() throws Exception {
        QueryResult<Long> count = variantSourceEvaproDBAdaptor.countSources();
        assertEquals(1, count.getNumResults());
        assertEquals(3, count.getResult().get(0).longValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getAllSources() throws Exception {
        variantSourceEvaproDBAdaptor.getAllSources(new QueryOptions());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getAllSourcesByStudyId() throws Exception {
        variantSourceEvaproDBAdaptor.getAllSourcesByStudyId("s1", new QueryOptions());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getAllSourcesByStudyIds() throws Exception {
        variantSourceEvaproDBAdaptor.getAllSourcesByStudyIds(Arrays.asList("s1", "s2"), new QueryOptions());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSamplesBySource() throws Exception {
        variantSourceEvaproDBAdaptor.getSamplesBySource("source", new QueryOptions());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSamplesBySources() throws Exception {
        variantSourceEvaproDBAdaptor.getSamplesBySources(Arrays.asList("s1", "s2"), new QueryOptions());
    }

    @Test
    public void getSourceDownloadUrlByName() throws Exception {
        QueryResult<URL> sourceUrls = variantSourceEvaproDBAdaptor.getSourceDownloadUrlByName(FileTestData.FILE_1_NAME);

        assertEquals(1, sourceUrls.getNumTotalResults());
        assertEquals(new URI("ftp://parentdir/dir1/file1.vcf.gz").toURL(), sourceUrls.getResult().get(0));
    }

    @Test
    public void getSourceDownloadUrlByNameFileNotInBrowsableFiles() throws Exception {
        QueryResult<URL> sourceUrls = variantSourceEvaproDBAdaptor
                .getSourceDownloadUrlByName(FileTestData.FILE_NOT_BROWSABLE);

        assertEquals(0, sourceUrls.getNumTotalResults());
    }

    @Test
    public void getSourceDownloadUrlByListOfNames() throws Exception {
        List<QueryResult> sourceUrls = variantSourceEvaproDBAdaptor
                .getSourceDownloadUrlByName(Arrays.asList(FileTestData.FILE_1_NAME, FileTestData.FILE_2_NAME, FileTestData.FILE_2_TABIX_NAME));

        assertEquals(2, sourceUrls.size());
        URL expectedFtpUrlFile1 = new URI("ftp://parentdir/dir1/file1.vcf.gz").toURL();
        URL expectedFtpUrlFile2 = new URI("ftp://parentdir/dir2/file2.vcf.gz").toURL();
        assertTrue(sourceUrls.stream()
                             .anyMatch(queryResult -> queryResult.getResult().get(0).equals(expectedFtpUrlFile1)));
        assertTrue(sourceUrls.stream()
                             .anyMatch(queryResult -> queryResult.getResult().get(0).equals(expectedFtpUrlFile2)));

    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSourceDownloadUrlById() throws Exception {
        variantSourceEvaproDBAdaptor.getSourceDownloadUrlById("s1", "s2");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void updateSourceStats() throws Exception {
        variantSourceEvaproDBAdaptor.updateSourceStats(new VariantSourceStats("f1", "s1"), new QueryOptions());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void close() throws Exception {
        variantSourceEvaproDBAdaptor.close();
    }

}