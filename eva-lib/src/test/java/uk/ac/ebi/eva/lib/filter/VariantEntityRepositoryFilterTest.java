package uk.ac.ebi.eva.lib.filter;

import org.junit.Test;

import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class VariantEntityRepositoryFilterTest {

    @Test
    public void getValueFromRelation() throws Exception {
        assertEquals(new Double(0.5), VariantEntityRepositoryFilter.getValueFromRelation("=0.5"));
        assertEquals(new Double(0.12), VariantEntityRepositoryFilter.getValueFromRelation(">0.12"));
        assertEquals(new Double(0.134), VariantEntityRepositoryFilter.getValueFromRelation(">=0.134"));
        assertEquals(new Double(1.1), VariantEntityRepositoryFilter.getValueFromRelation("<1.1"));
        assertEquals(new Double(0.5), VariantEntityRepositoryFilter.getValueFromRelation("<=0.5"));
    }

    @Test
    public void getRelationalOperatorFromRelation() throws Exception {
        assertEquals(VariantEntityRepository.RelationalOperator.EQ,
                     VariantEntityRepositoryFilter.getRelationalOperatorFromRelation("=0.5"));
        assertEquals(VariantEntityRepository.RelationalOperator.GT,
                     VariantEntityRepositoryFilter.getRelationalOperatorFromRelation(">0.12"));
        assertEquals(VariantEntityRepository.RelationalOperator.GTE,
                     VariantEntityRepositoryFilter.getRelationalOperatorFromRelation(">=0.134"));
        assertEquals(VariantEntityRepository.RelationalOperator.LT,
                     VariantEntityRepositoryFilter.getRelationalOperatorFromRelation("<1.1"));
        assertEquals(VariantEntityRepository.RelationalOperator.LTE,
                     VariantEntityRepositoryFilter.getRelationalOperatorFromRelation("<=0.5"));
    }

}