package uk.ac.ebi.eva.server.ws;

import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;
import uk.ac.ebi.eva.server.Utils;

class VariantFilterValues {

    private VariantEntityRepository.RelationalOperator mafOperator = VariantEntityRepository.RelationalOperator.NONE;
    private Double mafvalue;
    private VariantEntityRepository.RelationalOperator polyphenScoreOperator;
    private Double polyphenScoreValue;
    private VariantEntityRepository.RelationalOperator siftScoreOperator;
    private Double siftScoreValue;

    public VariantFilterValues(String maf, String polyphenScore, String siftScore) {
        if (maf != null) {
            setMafFilter(maf);
        }
        if (polyphenScore != null) {
            setPolyphenFilter(polyphenScore);
        }
        if (siftScore != null) {
            setSiftFilter(siftScore);
        }
    }

    public VariantEntityRepository.RelationalOperator getMafOperator() {
        return mafOperator;
    }

    public Double getMafvalue() {
        return mafvalue;
    }

    private void setMafFilter(String maf) {
        this.mafOperator = Utils.getRelationalOperatorFromRelation(maf);
        this.mafvalue = Utils.getValueFromRelation(maf);
    }

    public VariantEntityRepository.RelationalOperator getPolyphenScoreOperator() {
        return polyphenScoreOperator;
    }

    public Double getPolyphenScoreValue() {
        return polyphenScoreValue;
    }

    private void setPolyphenFilter(String polyphenScore) {
        this.polyphenScoreOperator = Utils.getRelationalOperatorFromRelation(polyphenScore);
        this.polyphenScoreValue = Utils.getValueFromRelation(polyphenScore);
    }

    public VariantEntityRepository.RelationalOperator getSiftScoreOperator() {
        return siftScoreOperator;
    }

    public Double getSiftScoreValue() {
        return siftScoreValue;
    }

    private void setSiftFilter(String siftScore) {
        this.siftScoreOperator = Utils.getRelationalOperatorFromRelation(siftScore);
        this.siftScoreValue = Utils.getValueFromRelation(siftScore);
    }

}
