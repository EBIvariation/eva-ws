package uk.ac.ebi.eva.lib.utils;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

import java.util.Collection;

public class RepositoryFilter<T> {

    private String field;
    private T value;
    private VariantEntityRepository.RelationalOperator operator = VariantEntityRepository.RelationalOperator.NONE;

    public RepositoryFilter(String field, T value, String relationalOperator) {
        this.field = field;
        this.value = value;

        if (value instanceof Collection) {
            this.operator = VariantEntityRepository.RelationalOperator.IN;
        } else {
            setOperator(relationalOperator);
        }
    }

    public Query applyFilter(Query query) {
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

    private void setOperator(String relation) {
        String relationalOperatorString = relation.replaceAll("[^<>=]", "");

        switch (relationalOperatorString) {
            case "=":
                this.operator = VariantEntityRepository.RelationalOperator.EQ;
            case ">":
                this.operator = VariantEntityRepository.RelationalOperator.GT;
            case "<":
                this.operator = VariantEntityRepository.RelationalOperator.LT;
            case ">=":
                this.operator = VariantEntityRepository.RelationalOperator.GTE;
            case "<=":
                this.operator = VariantEntityRepository.RelationalOperator.LTE;
            default:
                throw new IllegalArgumentException();
        }
    }

}
