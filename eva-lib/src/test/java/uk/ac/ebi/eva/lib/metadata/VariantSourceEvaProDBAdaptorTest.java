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

import uk.ac.ebi.eva.lib.entity.File;

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

    public static final String FILE_1_NAME = "file1.vcf.gz";

    public static final String FILE_2_NAME = "file2.vcf.gz";

    public static final String FILE_NOT_BROWSABLE = "file.notBroswable.vcf.gz";

    @Before
    public void setUp() throws Exception {
        File file1 = new File(1L, "ERF1", FILE_1_NAME, "sd3245as8dasiu2345d", "/dir/path", "vcf", "submitted", 1, true,
                              "/parentdir/dir1/" + FILE_1_NAME, true, "EVAF1");
        File file2 = new File(2L, "ERF2", FILE_2_NAME, "zd32452343242345c", "/dir/path", "vcf", "submitted", 1, true,
                              "/parentdir/dir2/" + FILE_2_NAME, true, "EVAF2");
        File incompleteFile3 = new File(3L, "ERF3", FILE_NOT_BROWSABLE, "kd3345as234156456f", "/dir/path", "vcf",
                                        "submitted", 1, true, "/parentdir/dir2/" + FILE_NOT_BROWSABLE, true, "EVAF3");

        entityManager.persist(file1);
        entityManager.persist(file2);
    }

    @Test
    public void countSources() throws Exception {
        QueryResult<Long> count = variantSourceEvaproDBAdaptor.countSources();
        assertEquals(1, count.getNumResults());
        assertEquals(2, count.getResult().get(0).longValue());
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
        QueryResult<URL> sourceUrls = variantSourceEvaproDBAdaptor.getSourceDownloadUrlByName(FILE_1_NAME);

        assertEquals(1, sourceUrls.getNumTotalResults());
        assertEquals(new URI("ftp://parentdir/dir1/file1.vcf.gz").toURL(), sourceUrls.getResult().get(0));
    }

    @Test
    public void getSourceDownloadUrlByNameFileNotInBrowsableFiles() throws Exception {
        QueryResult<URL> sourceUrls = variantSourceEvaproDBAdaptor
                .getSourceDownloadUrlByName(FILE_NOT_BROWSABLE);

        assertEquals(0, sourceUrls.getNumTotalResults());
    }

    @Test
    public void getSourceDownloadUrlByListOfNames() throws Exception {
        List<QueryResult> sourceUrls = variantSourceEvaproDBAdaptor
                .getSourceDownloadUrlByName(Arrays.asList(FILE_1_NAME, FILE_2_NAME));

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