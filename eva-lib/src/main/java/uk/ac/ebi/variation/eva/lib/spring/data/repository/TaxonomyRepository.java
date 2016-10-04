package uk.ac.ebi.variation.eva.lib.spring.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.ac.ebi.variation.eva.lib.models.Assembly;
import uk.ac.ebi.variation.eva.lib.spring.data.entity.Taxonomy;

import java.util.List;

/**
 * Created by jorizci on 03/10/16.
 */
public interface TaxonomyRepository extends JpaRepository<Taxonomy,Long>{

    @Query(nativeQuery = true)
    List<Assembly> getSpecies();

}
