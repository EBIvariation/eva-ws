package uk.ac.ebi.eva.lib.utils;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RepositoryFilterTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void apply() throws Exception {

    }

    @Test
    public void getRepositoryFiltersAllNull() throws Exception {
        assertEquals(new ArrayList<RepositoryFilter>(),
                     RepositoryFilter.getRepositoryFilters(null, null, null, null, null));
    }

    @Test
    public void getRepositoryFiltersOne() throws Exception {
        List<RepositoryFilter> expectedFilters = new ArrayList<>();
        expectedFilters.add(new RepositoryFilter<>("st.maf",
                                                   0.5,
                                                   VariantEntityRepository.RelationalOperator.EQ));

        assertEquals(expectedFilters,
                     RepositoryFilter.getRepositoryFilters("=0.5", null, null, null, null));
    }

    @Test
    public void getValueFromRelation() throws Exception {
        assertEquals(new Double(0.5), RepositoryFilter.getValueFromRelation("=0.5"));
        assertEquals(new Double(0.12), RepositoryFilter.getValueFromRelation(">0.12"));
        assertEquals(new Double(0.134), RepositoryFilter.getValueFromRelation(">=0.134"));
        assertEquals(new Double(1.1), RepositoryFilter.getValueFromRelation("<1.1"));
        assertEquals(new Double(0.5), RepositoryFilter.getValueFromRelation("<=0.5"));
    }

    @Test
    public void getRelationalOperatorFromRelation() throws Exception {
        assertEquals(VariantEntityRepository.RelationalOperator.EQ,
                     RepositoryFilter.getRelationalOperatorFromRelation("=0.5"));
        assertEquals(VariantEntityRepository.RelationalOperator.GT,
                     RepositoryFilter.getRelationalOperatorFromRelation(">0.12"));
        assertEquals(VariantEntityRepository.RelationalOperator.GTE,
                     RepositoryFilter.getRelationalOperatorFromRelation(">=0.134"));
        assertEquals(VariantEntityRepository.RelationalOperator.LT,
                     RepositoryFilter.getRelationalOperatorFromRelation("<1.1"));
        assertEquals(VariantEntityRepository.RelationalOperator.LTE,
                     RepositoryFilter.getRelationalOperatorFromRelation("<=0.5"));
    }

}