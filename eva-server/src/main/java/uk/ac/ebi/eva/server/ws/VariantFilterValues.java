package uk.ac.ebi.eva.server.ws;

import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;
import uk.ac.ebi.eva.server.Utils;

class VariantFilterValues {

    private VariantEntityRepository.RelationalOperator mafOperator;
    private Double mafvalue;
    private VariantEntityRepository.RelationalOperator polyphenScoreOperator;
    private Double polyphenScoreValue;
    private VariantEntityRepository.RelationalOperator siftScoreOperator;
    private Double siftScoreValue;

    public VariantFilterValues(String maf, String polyphenScore, String siftScore) {
        setMafOperator(VariantEntityRepository.RelationalOperator.NONE);
        setSiftScoreOperator(VariantEntityRepository.RelationalOperator.NONE);
        setPolyphenScoreOperator(VariantEntityRepository.RelationalOperator.NONE);

        if (maf != null) {
            setMafOperator(Utils.getRelationalOperatorFromRelation(maf));
            setMafvalue(Utils.getValueFromRelation(maf));
        }

        if (polyphenScore != null) {
            setPolyphenScoreOperator(Utils.getRelationalOperatorFromRelation(polyphenScore));
            setPolyphenScoreValue(Utils.getValueFromRelation(polyphenScore));
        }

        if (siftScore != null) {
            setSiftScoreOperator(Utils.getRelationalOperatorFromRelation(siftScore));
            setSiftScoreValue(Utils.getValueFromRelation(siftScore));
        }
    }

    public VariantEntityRepository.RelationalOperator getMafOperator() {
        return mafOperator;
    }

    public void setMafOperator(VariantEntityRepository.RelationalOperator mafOperator) {
        this.mafOperator = mafOperator;
    }

    public Double getMafvalue() {
        return mafvalue;
    }

    public void setMafvalue(Double mafvalue) {
        this.mafvalue = mafvalue;
    }

    public VariantEntityRepository.RelationalOperator getPolyphenScoreOperator() {
        return polyphenScoreOperator;
    }

    public void setPolyphenScoreOperator(
            VariantEntityRepository.RelationalOperator polyphenScoreOperator) {
        this.polyphenScoreOperator = polyphenScoreOperator;
    }

    public Double getPolyphenScoreValue() {
        return polyphenScoreValue;
    }

    public void setPolyphenScoreValue(Double polyphenScoreValue) {
        this.polyphenScoreValue = polyphenScoreValue;
    }

    public VariantEntityRepository.RelationalOperator getSiftScoreOperator() {
        return siftScoreOperator;
    }

    public void setSiftScoreOperator(VariantEntityRepository.RelationalOperator siftScoreOperator) {
        this.siftScoreOperator = siftScoreOperator;
    }

    public Double getSiftScoreValue() {
        return siftScoreValue;
    }

    public void setSiftScoreValue(Double siftScoreValue) {
        this.siftScoreValue = siftScoreValue;
    }

}
