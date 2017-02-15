package uk.ac.ebi.eva.lib.metadata;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import uk.ac.ebi.eva.lib.entity.DgvaStudyBrowser;

public class DgvaStudyTestData {
    private final TestEntityManager entityManager;

    static final String HUMAN = "Human";

    static final String MOUSE = "Mouse";

    static final String CONTROL_SET = "Control Set";

    static final String COLLECTION = "Collection";

    static final String STUDY_1_ID = "estd1";

    DgvaStudyTestData(TestEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    void persistTestData() {
        DgvaStudyBrowser study1 = new DgvaStudyBrowser(STUDY_1_ID, 1000, 10, 100, "9606", HUMAN, "Homo sapiens", "1111",
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
}
