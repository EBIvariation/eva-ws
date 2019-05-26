package uk.ac.ebi.eva.server.ws.ga4gh;

import java.util.HashMap;

public class DatasetAllelResponse {
    String datasetId;
    boolean exists;
    BeaconError error;
    Long frequency;
    Long variantCount;
    Long callCount;
    Long sampleCount;
    String note;
    String externalUrl;
    HashMap<String,String> info;

    public DatasetAllelResponse(String datasetId, boolean exists) {
        this.datasetId = datasetId;
        this.exists = exists;
    }

    public DatasetAllelResponse(String datasetId, boolean exists, BeaconError error, Long frequency, Long variantCount, Long callCount, Long sampleCount, String note, String externalUrl, HashMap<String, String> info) {
        this.datasetId = datasetId;
        this.exists = exists;
        this.error = error;
        this.frequency = frequency;
        this.variantCount = variantCount;
        this.callCount = callCount;
        this.sampleCount = sampleCount;
        this.note = note;
        this.externalUrl = externalUrl;
        this.info = info;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public BeaconError getError() {
        return error;
    }

    public void setError(BeaconError error) {
        this.error = error;
    }

    public Long getFrequency() {
        return frequency;
    }

    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }

    public Long getVariantCount() {
        return variantCount;
    }

    public void setVariantCount(Long variantCount) {
        this.variantCount = variantCount;
    }

    public Long getCallCount() {
        return callCount;
    }

    public void setCallCount(Long callCount) {
        this.callCount = callCount;
    }

    public Long getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Long sampleCount) {
        this.sampleCount = sampleCount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public HashMap<String, String> getInfo() {
        return info;
    }

    public void setInfo(HashMap<String, String> info) {
        this.info = info;
    }
}
