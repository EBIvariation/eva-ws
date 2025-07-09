package uk.ac.ebi.eva.lib.models.rocrate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Dataset extends RoCrateEntity {

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
    private List<String> analysisAccessions;

    @JsonProperty("hasPart")
    private List<URL> fileUrls;

    @JsonProperty("comment")
    private List<Comment> additionalProperties;

    public Dataset() {}

    public Dataset(String accession, String name, String description, Date datePublished, String centerName,
                   List<String> publications, List<String> analysisAccessions, List<URL> fileUrls, Long taxonomyId,
                   String scientificName, String scope, String material, String sourceType) {
        super("https://www.ebi.ac.uk/eva/?eva-study=" + accession, "schema.org/Dataset");
        this.projectAccession = accession;
        this.name = name;
        this.description = description;
        this.datePublished = datePublished;
        this.license = "https://www.ebi.ac.uk/data-protection/privacy-notice/embl-ebi-public-website/";
        this.centerName = centerName;
        this.publications = publications;
        this.analysisAccessions = analysisAccessions;
        this.fileUrls = fileUrls;
        additionalProperties = new ArrayList<>();
        additionalProperties.add(new Comment("taxonomyId", "" + taxonomyId));
        additionalProperties.add(new Comment("scientificName", scientificName));
        additionalProperties.add(new Comment("scope", scope));
        additionalProperties.add(new Comment("material", material));
        additionalProperties.add(new Comment("sourceType", sourceType));
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

    public List<String> getAnalysisAccessions() {
        return analysisAccessions;
    }

    public List<URL> getFileUrls() {
        return fileUrls;
    }

    public List<Comment> getAdditionalProperties() {
        return additionalProperties;
    }
}
