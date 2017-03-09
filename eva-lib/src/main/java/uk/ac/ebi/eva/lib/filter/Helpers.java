package uk.ac.ebi.eva.lib.filter;

import java.util.ArrayList;
import java.util.List;

public class Helpers {
    public static List<VariantEntityRepositoryFilter> getVariantEntityRepositoryFilters(String maf,
                                                                                        String polyphenScore,
                                                                                        String siftScore,
                                                                                        List<String> studies,
                                                                                        List<String> consequenceType,
                                                                                        List<String> files) {
        List<VariantEntityRepositoryFilter> filters = new ArrayList<>();
        if (studies != null && !studies.isEmpty()) {
            filters.add(new VariantEntityRepositoryStudyFilter(studies));
        }
        if (consequenceType != null && !consequenceType.isEmpty()) {
            filters.add(new VariantEntityRepositoryConsequenceTypeFilter(consequenceType));
        }
        if (files != null && !files.isEmpty()) {
            filters.add(new VariantEntityRepositoryFileFilter(files));
        }
        if (maf != null){
            filters.add(new VariantEntityRepositoryMafFilter(maf));
        }
        if (polyphenScore != null){
            filters.add(new VariantEntityRepositoryPolyphenFilter(polyphenScore));
        }
        if (siftScore != null){
            filters.add(new VariantEntityRepositorySiftFilter(siftScore));
        }
        return filters;
    }
}
