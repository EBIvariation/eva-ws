package uk.ac.ebi.eva.server.repository;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;

import java.util.List;

interface VariantEntityRepositoryCustom {

    List<VariantEntity> findByChrAndStartWithMarginAndEndWithMargin(String chr, int start, int end);
}
