package uk.ac.ebi.eva.server;

import org.opencb.datastore.core.QueryOptions;
import org.springframework.data.domain.PageRequest;

import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

public class Utils {

    public static Double getValueFromRelation(String relation) {
        return Double.parseDouble(relation.replaceAll("[^\\d.]", ""));
    }

    public static VariantEntityRepository.RelationalOperator getRelationalOperatorFromRelation(String relation) {
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

    public static PageRequest getPageRequest(QueryOptions queryOptions) {
        int limit = (int) queryOptions.get("limit");
        int skip = (int) queryOptions.get("skip");

        int size = (limit < 0) ? 10 : limit;
        int page = (skip < 0) ? 0 : Math.floorDiv(skip, size);

        return new PageRequest(page, size);
    }

}
