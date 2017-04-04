package uk.ac.ebi.eva.server.ws.ga4gh;

public class GA4GHBeaconResponse {

    private String chromosome;

    private Integer start;

    private String allele;

    private String datasetIds;

    private boolean exists;

    private String errorMessage;

    public GA4GHBeaconResponse() {
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

    public String getChromosome() {
        return chromosome;
    }

    public Integer getStart() {
        return start;
    }

    public String getAllele() {
        return allele;
    }

    public String getDatasetIds() {
        return datasetIds;
    }

    public boolean isExists() {
        return exists;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public void setAllele(String allele) {
        this.allele = allele;
    }

    public void setDatasetIds(String datasetIds) {
        this.datasetIds = datasetIds;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
