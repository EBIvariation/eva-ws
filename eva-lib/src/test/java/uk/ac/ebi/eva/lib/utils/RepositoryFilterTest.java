package uk.ac.ebi.eva.lib.utils;

import org.junit.Test;

import uk.ac.ebi.eva.lib.filter.RepositoryFilter;
import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RepositoryFilterTest {

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
        assertEquals(expectedFilters, RepositoryFilter.getRepositoryFilters("=0.5", null, null, null, null));

        expectedFilters = new ArrayList<>();
        expectedFilters.add(new RepositoryFilter<>("annot.ct.polyphen.sc",
                                                   0.13,
                                                   VariantEntityRepository.RelationalOperator.GT));
        assertEquals(expectedFilters, RepositoryFilter.getRepositoryFilters(null, ">0.13", null, null, null));

        expectedFilters = new ArrayList<>();
        expectedFilters.add(new RepositoryFilter<>("annot.ct.sift.sc",
                                                   0.09,
                                                   VariantEntityRepository.RelationalOperator.LTE));
        assertEquals(expectedFilters, RepositoryFilter.getRepositoryFilters(null, null, "<=0.09", null, null));

        List<String> studies = new ArrayList<>();
        String testStudyId = "TEST_STUDY";
        studies.add(testStudyId);
        expectedFilters = new ArrayList<>();
        expectedFilters.add(new RepositoryFilter<>("files.sid",
                                                   studies,
                                                   VariantEntityRepository.RelationalOperator.IN));
        assertEquals(expectedFilters, RepositoryFilter.getRepositoryFilters(null, null, null, studies, null));

        List<String> conTypes = new ArrayList<>();
        String testConType = "SO:000123";
        conTypes.add(testConType);
        List<Integer> conTypesOut = new ArrayList<>();
        int testConTypeOut = 123;
        conTypesOut.add(testConTypeOut);
        expectedFilters = new ArrayList<>();
        expectedFilters.add(new RepositoryFilter<>("annot.ct.so",
                                                   conTypesOut,
                                                   VariantEntityRepository.RelationalOperator.IN));
        assertEquals(expectedFilters, RepositoryFilter.getRepositoryFilters(null, null, null, null, conTypes));
    }

    @Test
    public void getRepositoryFilterMulti() throws Exception {
        List<RepositoryFilter> expectedFilters = new ArrayList<>();
        expectedFilters.add(new RepositoryFilter<>("st.maf",
                                                   0.5,
                                                   VariantEntityRepository.RelationalOperator.EQ));
        expectedFilters.add(new RepositoryFilter<>("annot.ct.polyphen.sc",
                                                   0.13,
                                                   VariantEntityRepository.RelationalOperator.GT));
        assertEquals(expectedFilters, RepositoryFilter.getRepositoryFilters("=0.5", ">0.13", null, null, null));


        expectedFilters = new ArrayList<>();
        expectedFilters.add(new RepositoryFilter<>("annot.ct.sift.sc",
                                                   0.09,
                                                   VariantEntityRepository.RelationalOperator.LTE));
        List<String> studies = new ArrayList<>();
        String testStudyId = "TEST_STUDY";
        studies.add(testStudyId);
        expectedFilters.add(new RepositoryFilter<>("files.sid",
                                                   studies,
                                                   VariantEntityRepository.RelationalOperator.IN));
        assertEquals(expectedFilters, RepositoryFilter.getRepositoryFilters(null, null, "<=0.09", studies, null));

        List<String> conTypes = new ArrayList<>();
        String testConType = "SO:000123";
        conTypes.add(testConType);
        List<Integer> conTypesOut = new ArrayList<>();
        int testConTypeOut = 123;
        conTypesOut.add(testConTypeOut);
        expectedFilters = new ArrayList<>();
        expectedFilters.add(new RepositoryFilter<>("st.maf",
                                                   0.5,
                                                   VariantEntityRepository.RelationalOperator.EQ));
        expectedFilters.add(new RepositoryFilter<>("annot.ct.so",
                                                   conTypesOut,
                                                   VariantEntityRepository.RelationalOperator.IN));
        assertEquals(expectedFilters, RepositoryFilter.getRepositoryFilters("=0.5", null, null, null, conTypes));
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