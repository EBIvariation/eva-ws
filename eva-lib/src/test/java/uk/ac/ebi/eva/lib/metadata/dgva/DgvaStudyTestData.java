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
package uk.ac.ebi.eva.lib.metadata.dgva;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import uk.ac.ebi.eva.lib.entities.DgvaStudyBrowser;

import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.HOMO_SAPIENS;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.HUMAN;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.MOUSE;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.M_MUSCULUS;

final class DgvaStudyTestData {

    static final String CONTROL_SET = "Control Set";

    static final String COLLECTION = "Collection";

    static final String STUDY_1_ID = "estd1";


    private DgvaStudyTestData() {}

    static void persistTestData(TestEntityManager entityManager) {
        DgvaStudyBrowser study1 = new DgvaStudyBrowser(STUDY_1_ID, 1000, 10, 100, "9606", HUMAN, HOMO_SAPIENS, "1111",
                                                       "alias1", "Study 1", CONTROL_SET, "PRJ1", "www.study1.com",
                                                       "This is study 1", "Sequence alignment", "HMM", "Sequencing",
                                                       "Illumina", "GRCh38", "GCA_000001405.16");
        DgvaStudyBrowser study2= new DgvaStudyBrowser("estd2", 2000, 20, 200, "9606", HUMAN, HOMO_SAPIENS, "2222",
                                                      "alias2", "Study 2", COLLECTION, "PRJ2", "www.study2.com",
                                                      "This is study 2", "Sequence alignment", "HMM", "Sequencing",
                                                      "Illumina", "GRCh38", "GCA_000001405.16");
        DgvaStudyBrowser study3 = new DgvaStudyBrowser("estd3", 3000, 30, 300, "9606", MOUSE, M_MUSCULUS, "3333",
                                                       "alias3", "Study 3", CONTROL_SET, "PRJ3", "www.study3.com",
                                                       "This is study 3", "Sequence alignment", "HMM", "Sequencing",
                                                       "Illumina", "MGSCv37", "GCA_000001635.1");

        entityManager.persist(study1);
        entityManager.persist(study2);
        entityManager.persist(study3);
    }
}