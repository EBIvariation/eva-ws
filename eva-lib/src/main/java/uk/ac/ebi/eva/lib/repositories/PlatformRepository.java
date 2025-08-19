package uk.ac.ebi.eva.lib.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.ac.ebi.eva.lib.entities.Platform;

public interface PlatformRepository extends JpaRepository<Platform, Long> {

}
