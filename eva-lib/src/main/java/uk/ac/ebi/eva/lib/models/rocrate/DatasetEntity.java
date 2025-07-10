package uk.ac.ebi.eva.lib.models.rocrate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

public class DatasetEntity extends RoCrateEntity {

    private String name;

    private String description;

    private Date datePublished;

    private String license;

    @JsonProperty("creator")
    private String centerName;

    @JsonProperty("citation")
    private List<String> publications;

    @JsonProperty("identifier")
    private String projectAccession;

    @JsonProperty("processSequence")
    private List<Reference> analysisAccessions;

    @JsonProperty("hasPart")
    private List<Reference> fileUrls;

    @JsonProperty("comment")
    // Project-level properties that aren't in schema.org/Dataset are included as Comments
    private List<Reference> additionalProperties;

    public DatasetEntity() {}

    public DatasetEntity(String accession, String name, String description, Date datePublished, String centerName,
                         List<String> publications, List<Reference> analysisAccessions, List<Reference> fileUrls,
                         List<Reference> additionalProperties) {
        super("https://www.ebi.ac.uk/eva/?eva-study=" + accession, "Dataset");
        this.projectAccession = accession;
        this.name = name;
        this.description = description;
        this.datePublished = datePublished;
        this.license = "https://www.ebi.ac.uk/data-protection/privacy-notice/embl-ebi-public-website/";
        this.centerName = centerName;
        this.publications = publications;
        this.analysisAccessions = analysisAccessions;
        this.fileUrls = fileUrls;
        this.additionalProperties = additionalProperties;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getDatePublished() {
        return datePublished;
    }

    public String getLicense() {
        return license;
    }

    public String getCenterName() {
        return centerName;
    }

    public List<String> getPublications() {
        return publications;
    }

    public String getProjectAccession() {
        return projectAccession;
    }

    public List<Reference> getAnalysisAccessions() {
        return analysisAccessions;
    }

    public List<Reference> getFileUrls() {
        return fileUrls;
    }

    public List<Reference> getAdditionalProperties() {
        return additionalProperties;
    }
}
