/*
 * Copyright 2014-2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.lib.metadata;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import uk.ac.ebi.eva.lib.entities.File;

public final class FileTestData {

    public static final String FILE_1_NAME = "file1.vcf.gz";

    public static final String FILE_2_NAME = "file2.vcf.gz";

    public static final String FILE_2_TABIX_NAME = FILE_2_NAME + ".tbi";

    public static final String FILE_NOT_BROWSABLE = "file.notBroswable.vcf.gz";

    private FileTestData() {}

    public static void persistTestData(TestEntityManager entityManager) {
        File file1 = new File(1L, "ERF1", FILE_1_NAME, "sd3245as8dasiu2345d", "/dir/path", "vcf", "submitted", 1, true,
                              "/parentdir/dir1/" + FILE_1_NAME, true, "EVAF1");
        File file2 = new File(2L, "ERF2", FILE_2_NAME, "zd32452343242345c", "/dir/path", "vcf", "submitted", 1, true,
                              "/parentdir/dir2/" + FILE_2_NAME, true, "EVAF2");
        File tabixFile = new File(22L, "ERF2", FILE_2_TABIX_NAME, "zd32452343242345c", "/dir/path", "tabix", "submitted", 1, true,
                              "/parentdir/dir2/" + FILE_2_TABIX_NAME, true, "EVAF2");

        File incompleteFile3 = new File(3L, "ERF3", FILE_NOT_BROWSABLE, "kd3345as234156456f", "/dir/path", "vcf",
                                        "submitted", 1, true, "/parentdir/dir2/" + FILE_NOT_BROWSABLE, true, "EVAF3");

        entityManager.persist(file1);
        entityManager.persist(file2);
        entityManager.persist(tabixFile);
        entityManager.persist(incompleteFile3);
    }


}
