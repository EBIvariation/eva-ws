package uk.ac.ebi.eva.server.ws.ga4gh;

public class BeaconError {
    String errorCode;
    int errorMessage;

    public BeaconError(String errorCode, int errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(int errorMessage) {
        this.errorMessage = errorMessage;
    }
}
