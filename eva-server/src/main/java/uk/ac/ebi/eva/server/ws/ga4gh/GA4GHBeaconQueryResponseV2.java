package uk.ac.ebi.eva.server.ws.ga4gh;

import uk.ac.ebi.eva.commons.core.utils.BeaconAllelRequest;

import java.util.List;

public class GA4GHBeaconQueryResponseV2 {

    private String beaconId;
    String apiVersion;
    Boolean exists;
    BeaconAllelRequest allelRequest;
    BeaconError error;
    List<DatasetAllelResponse> datasetAllelResponses;

    public GA4GHBeaconQueryResponseV2(String beaconId, String apiVersion, Boolean exists, BeaconAllelRequest allelRequest, BeaconError error,List<DatasetAllelResponse> datasetAllelResponses) {
        this.beaconId = beaconId;
        this.apiVersion = apiVersion;
        this.exists = exists;
        this.allelRequest = allelRequest;
        this.error = error;
        this.datasetAllelResponses=datasetAllelResponses;
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

    public Boolean isExists() {
        return exists;
    }

    public void setExists(Boolean exists) {
        this.exists = exists;
    }

    public BeaconAllelRequest getAllelRequest() {
        return allelRequest;
    }

    public void setAllelRequest(BeaconAllelRequest allelRequest) {
        this.allelRequest = allelRequest;
    }

    public BeaconError getError() {
        return error;
    }

    public void setError(BeaconError error) {
        this.error = error;
    }
}
