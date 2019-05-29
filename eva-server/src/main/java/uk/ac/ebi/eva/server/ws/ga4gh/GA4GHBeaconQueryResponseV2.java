package uk.ac.ebi.eva.server.ws.ga4gh;

import uk.ac.ebi.eva.commons.core.utils.BeaconAlleleRequest;

import java.util.List;

public class GA4GHBeaconQueryResponseV2 {

    private String beaconId;
    String apiVersion;
    Boolean exists;
    BeaconAlleleRequestBody alleleRequest;
    BeaconError error;
    List<DatasetAlleleResponse> datasetAlleleResponses;

    public GA4GHBeaconQueryResponseV2(String beaconId, String apiVersion, Boolean exists, BeaconAlleleRequestBody alleleRequest, BeaconError error, List<DatasetAlleleResponse> datasetAlleleResponses) {
        this.beaconId = beaconId;
        this.apiVersion = apiVersion;
        this.exists = exists;
        this.alleleRequest = alleleRequest;
        this.error = error;
        this.datasetAlleleResponses = datasetAlleleResponses;
    }

    public String getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(String beaconId) {
        this.beaconId = beaconId;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public Boolean getExists() {
        return exists;
    }

    public void setExists(Boolean exists) {
        this.exists = exists;
    }

    public BeaconAlleleRequestBody getAlleleRequest() {
        return alleleRequest;
    }

    public void setAlleleRequest(BeaconAlleleRequestBody alleleRequest) {
        this.alleleRequest = alleleRequest;
    }

    public BeaconError getError() {
        return error;
    }

    public void setError(BeaconError error) {
        this.error = error;
    }

    public List<DatasetAlleleResponse> getDatasetAlleleResponses() {
        return datasetAlleleResponses;
    }

    public void setDatasetAlleleResponses(List<DatasetAlleleResponse> datasetAlleleResponses) {
        this.datasetAlleleResponses = datasetAlleleResponses;
    }
}
