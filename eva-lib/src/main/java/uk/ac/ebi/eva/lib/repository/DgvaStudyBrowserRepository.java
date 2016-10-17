package uk.ac.ebi.eva.lib.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.eva.lib.extension.ExtendedJpaRepositoryFunctions;
import uk.ac.ebi.eva.lib.entity.DgvaStudyBrowser;

/**
 * Created by jorizci on 28/09/16.
 */
@Transactional
public interface DgvaStudyBrowserRepository extends JpaRepository<DgvaStudyBrowser, String>, ExtendedJpaRepositoryFunctions<DgvaStudyBrowser, String>, JpaSpecificationExecutor<DgvaStudyBrowser> {

    public final static String COMMON_NAME = "commonName";
    public final static String STUDY_TYPE = "studyType";
    public final static String SCIENTIFIC_NAME = "scientificName";

}
