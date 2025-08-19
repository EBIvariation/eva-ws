package uk.ac.ebi.eva.lib.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.ac.ebi.eva.lib.entities.Sample;

public interface SampleRepository extends JpaRepository<Sample, Long> {

}
