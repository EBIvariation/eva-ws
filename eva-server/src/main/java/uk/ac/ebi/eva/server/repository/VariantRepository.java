package uk.ac.ebi.eva.server.repository;

import org.opencb.biodata.models.variant.Variant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author Tom Smith
 */
public interface VariantRepository extends MongoRepository<Variant, String> {

    List<Variant> findByIds(String id);
//
//    List<Variant> findByChromosomeAndStartAndReferenceAndAlternate(String chromosome, String start, String reference, String alternate);
//
//    List<Variant> findByChromosomeAndStartAndReference(String chromosome, String start, String reference);
//
//    boolean exists(String id);

}
