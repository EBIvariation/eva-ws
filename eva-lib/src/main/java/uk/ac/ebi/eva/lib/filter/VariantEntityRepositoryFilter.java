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

import org.springframework.data.mongodb.core.query.Criteria;

import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class VariantEntityRepositoryFilter<T> {

    private String field;
    private T value;
    private VariantEntityRepository.RelationalOperator operator;

    public VariantEntityRepositoryFilter(String field, T value, VariantEntityRepository.RelationalOperator operator) {
        this.field = field;
        this.value = value;
        this.operator = operator;
    }

    public Criteria createCriteria() {
        Criteria criteria = Criteria.where(field);

        switch (operator) {
            case EQ:
                criteria = criteria.is(value);
                break;
            case GT:
                criteria = criteria.gt(value);
                break;
            case LT:
                criteria = criteria.lt(value);
                break;
            case GTE:
                criteria = criteria.gte(value);
                break;
            case LTE:
                criteria = criteria.lte(value);
                break;
            case IN:
                criteria = criteria.in(value);
                break;
            default:
                throw new IllegalArgumentException();
        }

        return criteria;
    }

    public static List<VariantEntityRepositoryFilter> getRepositoryFilters(String maf, String polyphenScore, String siftScore,
                                                                           List<String> studies, List<String> consequenceType) {
        List<VariantEntityRepositoryFilter> filters = new ArrayList<>();

        VariantEntityRepositoryFilter filter;

        if (maf != null) {
            filter = new VariantEntityRepositoryFilter<>("st.maf", getValueFromRelation(maf), getRelationalOperatorFromRelation(maf));
            filters.add(filter);
        }
        if (polyphenScore != null) {
            filter = new VariantEntityRepositoryFilter<>("annot.ct.polyphen.sc", getValueFromRelation(polyphenScore),
                                                         getRelationalOperatorFromRelation(polyphenScore));
            filters.add(filter);
        }
        if (siftScore != null) {
            filter = new VariantEntityRepositoryFilter<>("annot.ct.sift.sc", getValueFromRelation(siftScore),
                                                         getRelationalOperatorFromRelation(siftScore));
            filters.add(filter);
        }
        if (studies != null && !studies.isEmpty()) {
            filter = new VariantEntityRepositoryFilter<>("files.sid", studies, VariantEntityRepository.RelationalOperator.IN);
            filters.add(filter);
        }
        if (consequenceType != null && !consequenceType.isEmpty()) {
            List<Integer> consequenceTypeConv = consequenceType.stream()
                                                               .map(c -> Integer
                                                                       .parseInt(c.replaceAll("[^\\d.]", ""), 10))
                                                               .collect(Collectors.toList());
            filter = new VariantEntityRepositoryFilter<>("annot.ct.so", consequenceTypeConv,
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        VariantEntityRepositoryFilter o1 = (VariantEntityRepositoryFilter) o;

        return (this.field.equals(o1.field) &&
                Objects.equals(this.value, o1.value) &&
                Objects.equals(this.operator, o1.operator));
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, value, operator);
    }

}