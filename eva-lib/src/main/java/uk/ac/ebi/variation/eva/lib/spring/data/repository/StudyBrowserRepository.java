package uk.ac.ebi.variation.eva.lib.spring.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uk.ac.ebi.variation.eva.lib.spring.data.entity.DgvaStudyBrowser;
import uk.ac.ebi.variation.eva.lib.spring.data.entity.StudyBrowser;
import uk.ac.ebi.variation.eva.lib.spring.data.extension.ExtendedJpaRepositoryFunctions;

/**
 * Created by jorizci on 03/10/16.
 */
public interface StudyBrowserRepository extends JpaRepository<StudyBrowser, String>, ExtendedJpaRepositoryFunctions<StudyBrowser, String>, JpaSpecificationExecutor<StudyBrowser>{

    public static final String COMMON_NAME = "common_name";
    public static final String SCIENTIFIC_NAME = "scientific_name";
    public static final String EXPERIMENT_TYPE = "experiment_type";
}
