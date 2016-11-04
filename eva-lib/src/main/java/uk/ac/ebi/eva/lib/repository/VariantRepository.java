package uk.ac.ebi.eva.lib.repository;

import org.opencb.biodata.models.variant.Variant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author Tom Smith
 */
public interface VariantRepository extends MongoRepository<Variant, String> {

    Variant findById(String id);

    boolean exists(String id);

}
