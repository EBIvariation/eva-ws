package uk.ac.ebi.variation.eva.lib.spring.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.variation.eva.lib.spring.data.entity.DgvaStudyBrowser;
import uk.ac.ebi.variation.eva.lib.spring.data.extension.ExtendedJpaRepositoryFunctions;

/**
 * Created by jorizci on 28/09/16.
 */
@Transactional
public interface DGvaStudyBrowserRepository extends JpaRepository<DgvaStudyBrowser, String>, ExtendedJpaRepositoryFunctions<DgvaStudyBrowser, String>, JpaSpecificationExecutor<DgvaStudyBrowser> {

    public final static String COMMON_NAME = "common_name";
    public final static String STUDY_TYPE = "study_type";
    public final static String SCIENTIFIC_NAME = "scientific_name";

}
