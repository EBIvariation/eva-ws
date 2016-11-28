package uk.ac.ebi.eva.server.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;

import java.util.List;

interface VariantEntityRepository extends MongoRepository<VariantEntity, String>, VariantEntityRepositoryCustom {

    List<VariantEntity> findByIds(String id);

    List<VariantEntity> findByChrAndStartWithMarginAndEndWithMargin(String chr, int start, int end);
}
