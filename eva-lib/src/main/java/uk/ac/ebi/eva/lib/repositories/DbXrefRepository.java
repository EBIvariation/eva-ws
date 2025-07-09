package uk.ac.ebi.eva.lib.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.ac.ebi.eva.lib.entities.DbXref;

public interface DbXrefRepository extends JpaRepository<DbXref, Long> {

}
