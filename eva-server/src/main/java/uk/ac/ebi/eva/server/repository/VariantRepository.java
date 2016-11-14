package uk.ac.ebi.eva.server.repository;

import org.opencb.biodata.models.variant.Variant;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Tom Smith
 */
public interface VariantRepository extends MongoRepository<Variant, String> {

    Variant findByIds(String id);

    boolean exists(String id);

}
