package uk.ac.ebi.eva.lib.filter;

import org.opencb.biodata.models.variant.Variant;

import java.util.ArrayList;
import java.util.List;

public class FilterBuilder {

    private List<VariantEntityRepositoryFilter> filters = new ArrayList<>();

    public List<VariantEntityRepositoryFilter> getEvaWsVariantEntityRepositoryFilters(String maf,
                                                                                      String polyphenScore,
                                                                                      String siftScore,
                                                                                      List<String> studies,
                                                                                      List<String> consequenceType) {
        return this.withMaf(maf)
                   .withPolyphenScore(polyphenScore)
                   .withSiftScore(siftScore)
                   .withStudies(studies)
                   .withConsequenceType(consequenceType)
                   .build();
    }

    public List<VariantEntityRepositoryFilter> build() {
        return filters;
    }

    public FilterBuilder withMaf(String maf) {
        if (maf != null){
            filters.add(new VariantEntityRepositoryMafFilter(maf));
        }
        return this;
    }

    public FilterBuilder withPolyphenScore(String polyphenScore) {
        if (polyphenScore != null){
            filters.add(new VariantEntityRepositoryPolyphenFilter(polyphenScore));
        }
        return this;
    }

    public FilterBuilder withSiftScore(String siftScore) {
        if (siftScore != null){
            filters.add(new VariantEntityRepositorySiftFilter(siftScore));
        }
        return this;
    }

    public FilterBuilder withStudies(List<String> studies) {
        if (studies != null && !studies.isEmpty()) {
            filters.add(new VariantEntityRepositoryStudyFilter(studies));
        }
        return this;
    }

    public FilterBuilder withConsequenceType(List<String> consequenceType) {
        if (consequenceType != null && !consequenceType.isEmpty()) {
            filters.add(new VariantEntityRepositoryConsequenceTypeFilter(consequenceType));
        }
        return this;
    }

    public FilterBuilder withFiles(List<String> files) {
        if (files != null && !files.isEmpty()) {
            filters.add(new VariantEntityRepositoryFileFilter(files));
        }
        return this;
    }

    public FilterBuilder withVariantTypes(List<Variant.VariantType> types) {
        if (types != null && !types.isEmpty()) {
            filters.add(new VariantEntityRepositoryTypeFilter(types));
        }
        return this;
    }

    public FilterBuilder withAlternates(List<String> alternates) {
        if (alternates != null && !alternates.isEmpty()) {
            filters.add(new VariantEntityRepositoryAlternateFilter(alternates));
        }
        return this;
    }
}
