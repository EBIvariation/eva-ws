package uk.ac.ebi.eva.server.ws.ga4gh;

public class GA4GHBeaconResponse {

    private String chromosome;

    private Integer start;

    private String allele;

    private String datasetIds;

    private boolean exists;

    private String errorMessage;

    GA4GHBeaconResponse() {
    }

    public GA4GHBeaconResponse(String chromosome, Integer start, String allele, String datasetIds, boolean exists) {
        this.chromosome = chromosome;
        this.start = start;
        this.allele = allele;
        this.datasetIds = datasetIds;
        this.exists = exists;
    }

    public GA4GHBeaconResponse(String chromosome, Integer start, String allele, String datasetIds, String errorMessage) {
        this.chromosome = chromosome;
        this.start = start;
        this.allele = allele;
        this.datasetIds = datasetIds;
        this.errorMessage = errorMessage;
    }

    public boolean isExists() {
        return exists;
    }
}
