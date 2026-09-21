/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.lib.metadata.eva;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.eva.commons.core.models.stats.VariantSourceStats;
import uk.ac.ebi.eva.lib.metadata.FileTestData;
import uk.ac.ebi.eva.lib.utils.QueryOptions;
import uk.ac.ebi.eva.lib.utils.QueryResult;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Sql({"classpath:eva-schema.sql", "classpath:eva-data.sql"})
public class VariantSourceEvaProDBAdaptorTest {


    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VariantSourceEvaProDBAdaptor variantSourceEvaproDBAdaptor;

    @BeforeEach
    public void setUp() throws Exception {
        FileTestData.persistTestData(entityManager);
    }

    @Test
    public void countSources() {
        QueryResult<Long> count = variantSourceEvaproDBAdaptor.countSources();
        assertEquals(1, count.getNumResults());
        assertEquals(3, count.first().longValue());
    }

    @Test
    public void getAllSources() {
        assertThrows(UnsupportedOperationException.class, () -> variantSourceEvaproDBAdaptor.getAllSources(new QueryOptions()));
    }

    @Test
    public void getAllSourcesByStudyId() {
        assertThrows(UnsupportedOperationException.class, () -> variantSourceEvaproDBAdaptor.getAllSourcesByStudyId("s1", new QueryOptions()));
    }

    @Test
    public void getAllSourcesByStudyIds() {
        assertThrows(UnsupportedOperationException.class, () -> variantSourceEvaproDBAdaptor.getAllSourcesByStudyIds(Arrays.asList("s1", "s2"), new QueryOptions()));
    }

    @Test
    public void getSamplesBySource() {
        assertThrows(UnsupportedOperationException.class, () -> variantSourceEvaproDBAdaptor.getSamplesBySource("source", new QueryOptions()));
    }

    @Test
    public void getSamplesBySources() {
        assertThrows(UnsupportedOperationException.class, () -> variantSourceEvaproDBAdaptor.getSamplesBySources(Arrays.asList("s1", "s2"), new QueryOptions()));
    }

    @Test
    public void getSourceDownloadUrlByName() throws Exception {
        QueryResult<URL> sourceUrls = variantSourceEvaproDBAdaptor.getSourceDownloadUrlByName(FileTestData.FILE_1_NAME);

        assertEquals(1, sourceUrls.getNumTotalResults());
        assertEquals(new URI("ftp://parentdir/dir1/file1.vcf.gz").toURL(), sourceUrls.first());
    }

    @Test
    public void getSourceDownloadUrlByNameFileNotInBrowsableFiles() {
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
                .anyMatch(queryResult -> queryResult.first().equals(expectedFtpUrlFile1)));
        assertTrue(sourceUrls.stream()
                .anyMatch(queryResult -> queryResult.first().equals(expectedFtpUrlFile2)));

    }

    @Test
    public void getSourceDownloadUrlById() {
        assertThrows(UnsupportedOperationException.class, () -> variantSourceEvaproDBAdaptor.getSourceDownloadUrlById("s1", "s2"));
    }

    @Test
    public void updateSourceStats() {
        assertThrows(UnsupportedOperationException.class, () -> variantSourceEvaproDBAdaptor.updateSourceStats(new VariantSourceStats("f1", "s1"), new QueryOptions()));
    }

    @Test
    public void close() {
        assertThrows(UnsupportedOperationException.class, () -> variantSourceEvaproDBAdaptor.close());
    }

}