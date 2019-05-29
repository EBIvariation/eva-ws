package uk.ac.ebi.eva.server.ws.ga4gh;

import javax.validation.constraints.NotNull;
import java.util.List;

public class BeaconAlleleRequestBody {
    @NotNull(message = "referenceName should not be null")
    String referenceName;
    Long start;
    Long startMin;
    Long startMax;
    Long end;
    Long endMin;
    Long endMax;
    @NotNull(message = "referenceBases should not be null")
    String referenceBases;
    String alternateBases;
    String variantType;
    @NotNull(message = "assemblyId should not be null")
    String assemblyId;
    List<String> datasetIds;
    String includeDatasetResponses;

    public BeaconAlleleRequestBody() {
    }

    public BeaconAlleleRequestBody(String referenceName, Long start, Long startMin, Long startMax, Long end, Long endMin, Long endMax, String referenceBases, String alternateBases, String variantType, String assemblyId, List<String> datasetIds, String includeDatasetResponses) {
        this.referenceName = referenceName;
        this.start = start;
        this.startMin = startMin;
        this.startMax = startMax;
        this.end = end;
        this.endMin = endMin;
        this.endMax = endMax;
        this.referenceBases = referenceBases;
        this.alternateBases = alternateBases;
        this.variantType = variantType;
        this.assemblyId = assemblyId;
        this.datasetIds = datasetIds;
        this.includeDatasetResponses = includeDatasetResponses;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getStartMin() {
        return startMin;
    }

    public void setStartMin(Long startMin) {
        this.startMin = startMin;
    }

    public Long getStartMax() {
        return startMax;
    }

    public void setStartMax(Long startMax) {
        this.startMax = startMax;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Long getEndMin() {
        return endMin;
    }

    public void setEndMin(Long endMin) {
        this.endMin = endMin;
    }

    public Long getEndMax() {
        return endMax;
    }

    public void setEndMax(Long endMax) {
        this.endMax = endMax;
    }

    public String getReferenceBases() {
        return referenceBases;
    }

    public void setReferenceBases(String referenceBases) {
        this.referenceBases = referenceBases;
    }

    public String getAlternateBases() {
        return alternateBases;
    }

    public void setAlternateBases(String alternateBases) {
        this.alternateBases = alternateBases;
    }

    public String getVariantType() {
        return variantType;
    }

    public void setVariantType(String variantType) {
        this.variantType = variantType;
    }

    public String getAssemblyId() {
        return assemblyId;
    }

    public void setAssemblyId(String assemblyId) {
        this.assemblyId = assemblyId;
    }

    public List<String> getDatasetIds() {
        return datasetIds;
    }

    public void setDatasetIds(List<String> datasetIds) {
        this.datasetIds = datasetIds;
    }

    public String getIncludeDatasetResponses() {
        return includeDatasetResponses;
    }

    public void setIncludeDatasetResponses(String includeDatasetResponses) {
        this.includeDatasetResponses = includeDatasetResponses;
    }
}
