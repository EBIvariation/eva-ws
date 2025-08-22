package uk.ac.ebi.eva.lib.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.ac.ebi.eva.lib.entities.Analysis;

public interface AnalysisRepository extends JpaRepository<Analysis, String> {

}
