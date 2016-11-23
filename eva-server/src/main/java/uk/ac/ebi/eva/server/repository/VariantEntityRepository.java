package uk.ac.ebi.eva.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;

import java.util.List;

public interface VariantEntityRepository extends MongoRepository<VariantEntity, String> {

    int MARGIN = 1000000;

    List<VariantEntity> findByIds(String id);

    @Query("{ 'chr' : ?0, 'start' : { $lt : ?2, $gt : $1 - " + VariantEntityRepository.MARGIN + " } , 'end' : { $gt : ?1, $lt : $2 + " + VariantEntityRepository.MARGIN + " }}")
    List<VariantEntity> findByChrAndStartWithMarginAndEndWithMargin(String chromosome, int start, int end);

}
