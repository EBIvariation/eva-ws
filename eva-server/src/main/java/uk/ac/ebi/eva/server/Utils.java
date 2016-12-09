package uk.ac.ebi.eva.server;

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

}
