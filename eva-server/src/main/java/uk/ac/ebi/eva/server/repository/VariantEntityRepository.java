package uk.ac.ebi.eva.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;

import java.util.List;

/**
 * @author Tom Smith
 */
public interface VariantEntityRepository extends MongoRepository<VariantEntity, String> {

    List<VariantEntity> findByIds(String id);

//    List<VariantEntity> findByChromosomeAndStartAndReferenceAndAlternate(String chromosome, String start, String reference, String alternate);
//
//    List<VariantEntity> findByChromosomeAndStartAndReference(String chromosome, String start, String reference);
//
//    boolean exists(String id);

}
