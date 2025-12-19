package uk.ac.ebi.eva.lib.models.rocrate;

import com.fasterxml.jackson.annotation.JsonProperty;


import java.time.LocalDate;
import java.util.List;

public class ProjectDatasetEntity extends MinimalProjectDatasetEntity {

    @JsonProperty("creator")
    private String centerName;

    @JsonProperty("citation")
    private List<String> publications;

    @JsonProperty("processSequence")
    private List<Reference> analyses;

    @JsonProperty("hasPart")
    private List<Reference> files;

    public ProjectDatasetEntity() {
    }

    public ProjectDatasetEntity(String accession, String name, String description, LocalDate datePublished, String centerName,
                                List<String> publications, List<Reference> analyses, List<Reference> files,
                                List<Reference> additionalProperties) {
        super(accession, name, description, datePublished, additionalProperties);
        this.centerName = centerName;
        this.publications = publications;
        this.analyses = analyses;
        this.files = files;
    }

    public String getCenterName() {
        return centerName;
    }

    public List<String> getPublications() {
        return publications;
    }

    public List<Reference> getAnalyses() {
        return analyses;
    }

    public List<Reference> getFiles() {
        return files;
    }

}
