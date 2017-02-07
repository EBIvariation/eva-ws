package uk.ac.ebi.eva.lib.filter;

import org.junit.Test;

import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class VariantEntityRepositoryFilterTest {

    @Test
    public void getRepositoryFiltersAllNull() throws Exception {
        assertEquals(new ArrayList<VariantEntityRepositoryFilter>(),
                     VariantEntityRepositoryFilter.getRepositoryFilters(null, null, null, null, null));
    }

    @Test
    public void getRepositoryFiltersOne() throws Exception {
        List<VariantEntityRepositoryFilter> expectedFilters = new ArrayList<>();
        expectedFilters.add(new VariantEntityRepositoryFilter<>("st.maf",
                                                                0.5,
                                                                VariantEntityRepository.RelationalOperator.EQ));
        assertEquals(expectedFilters, VariantEntityRepositoryFilter.getRepositoryFilters("=0.5", null, null, null, null));

        expectedFilters = new ArrayList<>();
        expectedFilters.add(new VariantEntityRepositoryFilter<>("annot.ct.polyphen.sc",
                                                                0.13,
                                                                VariantEntityRepository.RelationalOperator.GT));
        assertEquals(expectedFilters, VariantEntityRepositoryFilter.getRepositoryFilters(null, ">0.13", null, null, null));

        expectedFilters = new ArrayList<>();
        expectedFilters.add(new VariantEntityRepositoryFilter<>("annot.ct.sift.sc",
                                                                0.09,
                                                                VariantEntityRepository.RelationalOperator.LTE));
        assertEquals(expectedFilters, VariantEntityRepositoryFilter
                .getRepositoryFilters(null, null, "<=0.09", null, null));

        List<String> studies = new ArrayList<>();
        String testStudyId = "TEST_STUDY";
        studies.add(testStudyId);
        expectedFilters = new ArrayList<>();
        expectedFilters.add(new VariantEntityRepositoryFilter<>("files.sid",
                                                                studies,
                                                                VariantEntityRepository.RelationalOperator.IN));
        assertEquals(expectedFilters, VariantEntityRepositoryFilter.getRepositoryFilters(null, null, null, studies, null));

        List<String> conTypes = new ArrayList<>();
        String testConType = "SO:000123";
        conTypes.add(testConType);
        List<Integer> conTypesOut = new ArrayList<>();
        int testConTypeOut = 123;
        conTypesOut.add(testConTypeOut);
        expectedFilters = new ArrayList<>();
        expectedFilters.add(new VariantEntityRepositoryFilter<>("annot.ct.so",
                                                                conTypesOut,
                                                                VariantEntityRepository.RelationalOperator.IN));
        assertEquals(expectedFilters, VariantEntityRepositoryFilter
                .getRepositoryFilters(null, null, null, null, conTypes));
    }

    @Test
    public void getRepositoryFilterMulti() throws Exception {
        List<VariantEntityRepositoryFilter> expectedFilters = new ArrayList<>();
        expectedFilters.add(new VariantEntityRepositoryFilter<>("st.maf",
                                                                0.5,
                                                                VariantEntityRepository.RelationalOperator.EQ));
        expectedFilters.add(new VariantEntityRepositoryFilter<>("annot.ct.polyphen.sc",
                                                                0.13,
                                                                VariantEntityRepository.RelationalOperator.GT));
        assertEquals(expectedFilters, VariantEntityRepositoryFilter
                .getRepositoryFilters("=0.5", ">0.13", null, null, null));


        expectedFilters = new ArrayList<>();
        expectedFilters.add(new VariantEntityRepositoryFilter<>("annot.ct.sift.sc",
                                                                0.09,
                                                                VariantEntityRepository.RelationalOperator.LTE));
        List<String> studies = new ArrayList<>();
        String testStudyId = "TEST_STUDY";
        studies.add(testStudyId);
        expectedFilters.add(new VariantEntityRepositoryFilter<>("files.sid",
                                                                studies,
                                                                VariantEntityRepository.RelationalOperator.IN));
        assertEquals(expectedFilters, VariantEntityRepositoryFilter
                .getRepositoryFilters(null, null, "<=0.09", studies, null));

        List<String> conTypes = new ArrayList<>();
        String testConType = "SO:000123";
        conTypes.add(testConType);
        List<Integer> conTypesOut = new ArrayList<>();
        int testConTypeOut = 123;
        conTypesOut.add(testConTypeOut);
        expectedFilters = new ArrayList<>();
        expectedFilters.add(new VariantEntityRepositoryFilter<>("st.maf",
                                                                0.5,
                                                                VariantEntityRepository.RelationalOperator.EQ));
        expectedFilters.add(new VariantEntityRepositoryFilter<>("annot.ct.so",
                                                                conTypesOut,
                                                                VariantEntityRepository.RelationalOperator.IN));
        assertEquals(expectedFilters, VariantEntityRepositoryFilter
                .getRepositoryFilters("=0.5", null, null, null, conTypes));
    }

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