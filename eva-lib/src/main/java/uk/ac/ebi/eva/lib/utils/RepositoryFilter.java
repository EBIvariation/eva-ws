package uk.ac.ebi.eva.lib.utils;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RepositoryFilter<T> {

    private String field;
    private T value;
    private VariantEntityRepository.RelationalOperator operator = VariantEntityRepository.RelationalOperator.NONE;

    public RepositoryFilter(String field, T value, VariantEntityRepository.RelationalOperator operator) {
        this.field = field;
        this.value = value;
        this.operator = operator;
    }

    public Query apply(Query query) {
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
            case NONE:
                throw new IllegalArgumentException();
        }

        query.addCriteria(criteria);
        return query;
    }

    public static List<RepositoryFilter> getRepositoryFilters(String maf, String polyphenScore, String siftScore,
                                                              List<String> studies, List<String> consequenceType) {
        List<RepositoryFilter> filters = new ArrayList<>();

        RepositoryFilter filter;

        if (maf != null) {
            filter = new RepositoryFilter<>("st.maf", getValueFromRelation(maf), getRelationalOperatorFromRelation(maf));
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

        RepositoryFilter o1 = (RepositoryFilter) o;

        return (this.field.equals(o1.field) &&
                Objects.equals(this.value, o1.value) &&
                Objects.equals(this.operator, o1.operator));
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, value, operator);
    }

}
