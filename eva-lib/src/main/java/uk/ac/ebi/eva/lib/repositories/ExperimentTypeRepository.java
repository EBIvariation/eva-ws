package uk.ac.ebi.eva.lib.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.ac.ebi.eva.lib.entities.ExperimentType;

public interface ExperimentTypeRepository extends JpaRepository<ExperimentType, Long> {

}
