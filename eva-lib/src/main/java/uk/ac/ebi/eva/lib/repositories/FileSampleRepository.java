package uk.ac.ebi.eva.lib.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.ac.ebi.eva.lib.entities.FileSample;
import uk.ac.ebi.eva.lib.entities.FileSamplePK;

public interface FileSampleRepository extends JpaRepository<FileSample, FileSamplePK> {

}
