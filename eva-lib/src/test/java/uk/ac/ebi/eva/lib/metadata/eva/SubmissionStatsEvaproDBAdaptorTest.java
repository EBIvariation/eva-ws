/*
 * Copyright 2025 EMBL - European Bioinformatics Institute
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.lib.entities.Analysis;
import uk.ac.ebi.eva.lib.entities.File;
import uk.ac.ebi.eva.lib.entities.Submission;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class SubmissionStatsEvaproDBAdaptorTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SubmissionStatsEvaproDBAdaptor submissionStatsEvaproDBAdaptor;

    @Before
    public void setUp() {
        // SUB001: January 2023, files of size 100 and 200 → SUM 300
        Submission sub1 = new Submission(1L, "ERA001", "STUDY", "ADD", "Submission 1", null,
                                         LocalDate.of(2023, 1, 15), 0);
        File file1 = new File(101L, "ERF101", "file1.vcf.gz", "md5_1", "/dir", "vcf", "submitted",
                              1, true, "/ftp/file1.vcf.gz", true, "EVAF101");
        file1.setFileSize(100L);
        File file2 = new File(102L, "ERF102", "file2.vcf.gz", "md5_2", "/dir", "vcf", "submitted",
                              1, true, "/ftp/file2.vcf.gz", true, "EVAF102");
        file2.setFileSize(200L);
        Analysis analysis1 = new Analysis("ERZ001", "Analysis 1", "alias1", "desc1", "EBI",
                                          null, null, null, 0, null);
        entityManager.persist(sub1);
        entityManager.persist(file1);
        entityManager.persist(file2);
        analysis1.setSubmission(sub1);
        analysis1.setFiles(Arrays.asList(file1, file2));
        entityManager.persist(analysis1);

        // SUB002: January 2023, file of size 500 → SUM 500
        Submission sub2 = new Submission(2L, "ERA002", "STUDY", "ADD", "Submission 2", null,
                                         LocalDate.of(2023, 1, 20), 0);
        File file3 = new File(103L, "ERF103", "file3.vcf.gz", "md5_3", "/dir", "vcf", "submitted",
                              1, true, "/ftp/file3.vcf.gz", true, "EVAF103");
        file3.setFileSize(500L);
        Analysis analysis2 = new Analysis("ERZ002", "Analysis 2", "alias2", "desc2", "EBI",
                                          null, null, null, 0, null);
        entityManager.persist(sub2);
        entityManager.persist(file3);
        analysis2.setSubmission(sub2);
        analysis2.setFiles(Collections.singletonList(file3));
        entityManager.persist(analysis2);

        // SUB003: February 2023, file with null size → SUM null → treated as 0
        Submission sub3 = new Submission(3L, "ERA003", "STUDY", "ADD", "Submission 3", null,
                                         LocalDate.of(2023, 2, 10), 0);
        File file4 = new File(104L, "ERF104", "file4.vcf.gz", "md5_4", "/dir", "vcf", "submitted",
                              1, true, "/ftp/file4.vcf.gz", true, "EVAF104");
        // file4 has no file size (null), so SUM will be null → 0L
        Analysis analysis3 = new Analysis("ERZ003", "Analysis 3", "alias3", "desc3", "EBI",
                                          null, null, null, 0, null);
        entityManager.persist(sub3);
        entityManager.persist(file4);
        analysis3.setSubmission(sub3);
        analysis3.setFiles(Collections.singletonList(file4));
        entityManager.persist(analysis3);

        // SUB004: null date → filtered out from results in both methods
        Submission sub4 = new Submission(4L, "ERA004", "STUDY", "ADD", "Submission 4", null,
                                         null, 0);
        File file5 = new File(105L, "ERF105", "file5.vcf.gz", "md5_5", "/dir", "vcf", "submitted",
                              1, true, "/ftp/file5.vcf.gz", true, "EVAF105");
        file5.setFileSize(50L);
        Analysis analysis4 = new Analysis("ERZ004", "Analysis 4", "alias4", "desc4", "EBI",
                                          null, null, null, 0, null);
        entityManager.persist(sub4);
        entityManager.persist(file5);
        analysis4.setSubmission(sub4);
        analysis4.setFiles(Collections.singletonList(file5));
        entityManager.persist(analysis4);

        entityManager.flush();
    }

    @After
    public void tearDown() {
        entityManager.clear();
    }

    @Test
    public void getCountByMonth() {
        Map<String, Long> result = submissionStatsEvaproDBAdaptor.getCountByMonth();

        assertEquals(2, result.size());
        // SUB001 and SUB002 are both in January 2023
        assertEquals(2L, (long) result.get("202301"));
        // SUB003 is in February 2023
        assertEquals(1L, (long) result.get("202302"));
    }

    @Test
    public void getBytesByMonth() {
        Map<String, Long> result = submissionStatsEvaproDBAdaptor.getBytesByMonth();

        assertEquals(2, result.size());
        // SUB001: 100 + 200 = 300; SUB002: 500; total for January = 800
        assertEquals(800L, (long) result.get("202301"));
        // SUB003: file size is null → 0
        assertEquals(0L, (long) result.get("202302"));
    }
}
