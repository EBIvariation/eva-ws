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

import uk.ac.ebi.eva.lib.entities.EvaStudyBrowser;

import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.BOS_TAURUS;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.CATTLE;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.HOMO_SAPIENS;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.HUMAN;

final class EvaStudyBrowserTestData {

    static final String WHOLE_GENOME_SEQUENCING = "Whole genome sequencing";

    static final String RNA_SEQ = "RNA sequencing";

    static final String EXOME_SEQUENCING = "Exome sequencing";

    static final String PROJECT_ID_1 = "PRJ0001";

    private EvaStudyBrowserTestData() {}

    static void persistTestData(TestEntityManager entityManager) {
        EvaStudyBrowser humanWGSStudy1 = new EvaStudyBrowser(PROJECT_ID_1, 1, "Project 1", "This is project 1", "1111",
                                                             HUMAN, HOMO_SAPIENS, "source 1",
                                                             "Whole genome sequencing", 1000000L, 1000, "center 1",
                                                             "scope 1", "DNA", "PubMed:1111", "PRJ00002",
                                                             WHOLE_GENOME_SEQUENCING, "WGS", "GCA_000001405.22",
                                                             "GRCh38.p7",
                                                             "Illumina", "Resource1", false);
        EvaStudyBrowser humanWGSStudy2 = new EvaStudyBrowser("PRJ0002", 2, "Project 2", "This is project 2", "2222",
                                                             HUMAN, HOMO_SAPIENS, "source 2",
                                                             "Whole genome sequencing", 2000000L, 2000, "center 2",
                                                             "scope 2", "DNA", "PubMed:2222", "PRJ00002",
                                                             RNA_SEQ, "WGS", "GCA_000001405.22", "GRCh38.p7",
                                                             "Illumina", "Resource2", false);
        EvaStudyBrowser humanESStudy = new EvaStudyBrowser("PRJ0003", 3, "Project 3", "This is project 3", "3333",
                                                           HUMAN, HOMO_SAPIENS, "source 3", EXOME_SEQUENCING,
                                                           3000000L, 3000, "center 3", "scope 3", "DNA", "PubMed:3333",
                                                           "PRJ00003", EXOME_SEQUENCING, "ES", "GCA_000001405.14",
                                                           "GRCh37.p13", "Illumina", "Resource3", false);
        EvaStudyBrowser cowESStudy = new EvaStudyBrowser("PRJ0004", 4, "Project 4", "This is project 4", "4444", CATTLE,
                                                         BOS_TAURUS, "source 4", EXOME_SEQUENCING, 4000000L, 4000,
                                                         "center 4", "scope 4", "DNA", "PubMed:4444", "PRJ00004",
                                                         EXOME_SEQUENCING, "ES", "GCA_000003055.3", "UMD3.1",
                                                         "Illumina", "Resource4", false);
        entityManager.persist(humanWGSStudy1);
        entityManager.persist(humanWGSStudy2);
        entityManager.persist(humanESStudy);
        entityManager.persist(cowESStudy);
    }
}
