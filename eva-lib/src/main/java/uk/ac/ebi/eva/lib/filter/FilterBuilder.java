/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.lib.filter;

import org.opencb.biodata.models.variant.Variant;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for building filters for querying using the VariantEntityRepository
 */
public class FilterBuilder {

    private List<VariantEntityRepositoryFilter> filters = new ArrayList<>();

    public List<VariantEntityRepositoryFilter> getVariantEntityRepositoryFilters(String maf,
                                                                                 String polyphenScore,
                                                                                 String siftScore,
                                                                                 List<String> studies,
                                                                                 List<String> consequenceType,
                                                                                 String ref, List<String> alts,
                                                                                 List<String> xrefIds) {
        return this.withMaf(maf)
                   .withPolyphenScore(polyphenScore)
                   .withSiftScore(siftScore)
                   .withStudies(studies)
                   .withConsequenceType(consequenceType)
                   .withRef(ref)
                   .withAlternates(alts)
                   .withXrefsIds(xrefIds)
                   .build();
    }

    public List<VariantEntityRepositoryFilter> build() {
        return filters;
    }

    public FilterBuilder withMaf(String maf) {
        if (maf != null) {
            filters.add(new VariantEntityRepositoryMafFilter(maf));
        }
        return this;
    }

    public FilterBuilder withPolyphenScore(String polyphenScore) {
        if (polyphenScore != null) {
            filters.add(new VariantEntityRepositoryPolyphenFilter(polyphenScore));
        }
        return this;
    }

    public FilterBuilder withSiftScore(String siftScore) {
        if (siftScore != null) {
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

    public FilterBuilder withXrefsIds(List<String> xrefsIds) {
        if (xrefsIds != null && !xrefsIds.isEmpty()) {
            filters.add(new VariantEntityRepositoryXrefsIdFilter(xrefsIds));
        }
        return this;
    }

    public FilterBuilder withRef(String ref) {
        if (ref != null) {
            filters.add(new VariantEntityRepositoryReferenceFilter(ref));
        }
        return this;
    }
}
