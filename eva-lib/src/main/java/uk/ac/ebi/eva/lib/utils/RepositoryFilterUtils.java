package uk.ac.ebi.eva.lib.utils;

import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RepositoryFilterUtils {

    public static List<RepositoryFilter> getRepositoryFilters(String maf, String polyphenScore, String siftScore,
                                                              List<String> studies, List<String> consequenceType) {
        List<RepositoryFilter> filters = new ArrayList<>();

        RepositoryFilter filter;

        if (maf != null) {
            filter = new RepositoryFilter<>("st.maf", getValueFromRelation(maf),
                                            getRelationalOperatorFromRelation(maf));
            filters.add(filter);
        }
        if (polyphenScore != null) {
            filter = new RepositoryFilter<>("annot.ct.polyphen.sc", getValueFromRelation(polyphenScore),
                                            getRelationalOperatorFromRelation(polyphenScore));
            filters.add(filter);
        }
        if (siftScore != null) {
            filter = new RepositoryFilter<>("annot.ct.sift.sc", getValueFromRelation(siftScore),
                                            getRelationalOperatorFromRelation(siftScore));
            filters.add(filter);
        }
        if (studies != null && !studies.isEmpty()) {
            filter = new RepositoryFilter<>("files.sid", studies, VariantEntityRepository.RelationalOperator.IN);
            filters.add(filter);
        }
        if (consequenceType != null && !consequenceType.isEmpty()) {
            List<Integer> consequenceTypeConv = consequenceType.stream()
                                                               .map(c -> Integer
                                                                       .parseInt(c.replaceAll("[^\\d.]", ""), 10))
                                                               .collect(Collectors.toList());
            filter = new RepositoryFilter<>("annot.ct.so", consequenceTypeConv,
                                            VariantEntityRepository.RelationalOperator.IN);
            filters.add(filter);
        }
        return filters;
    }

    static Double getValueFromRelation(String relation) {
        return Double.parseDouble(relation.replaceAll("[^\\d.]", ""));
    }

    static VariantEntityRepository.RelationalOperator getRelationalOperatorFromRelation(String relation) {
        String relationalOperatorString = relation.replaceAll("[^<>=]", "");

        switch (relationalOperatorString) {
            case "=":
                return VariantEntityRepository.RelationalOperator.EQ;
            case ">":
                return VariantEntityRepository.RelationalOperator.GT;
            case "<":
                return VariantEntityRepository.RelationalOperator.LT;
            case ">=":
                return VariantEntityRepository.RelationalOperator.GTE;
            case "<=":
                return VariantEntityRepository.RelationalOperator.LTE;
            default:
                throw new IllegalArgumentException();
        }

    }
}
