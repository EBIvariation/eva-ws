package uk.ac.ebi.eva.lib.spring.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.eva.lib.spring.data.entity.Project;

/**
 * Created by jorizci on 03/10/16.
 */
public interface ProjectRepository extends JpaRepository<Project,String > {
}
