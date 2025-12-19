package uk.ac.ebi.eva.lib.models.rocrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.time.LocalDate;
import java.util.List;

public class MinimalProjectDatasetEntity extends RoCrateEntity {

    protected String name;

    protected String description;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    protected LocalDate datePublished;

    protected String license;

    @JsonProperty("identifier")
    protected String projectAccession;

    @JsonProperty("comment")
    // Project-level properties that aren't in schema.org/Dataset are included as Comments
    protected List<Reference> additionalProperties;

    public MinimalProjectDatasetEntity() {
    }

    public MinimalProjectDatasetEntity(String accession, String name, String description, LocalDate datePublished,
                                       List<Reference> additionalProperties) {
        super("https://www.ebi.ac.uk/eva/?eva-study=" + accession, "Dataset");
        this.projectAccession = accession;
        this.name = name;
        this.description = description;
        this.datePublished = datePublished;
        this.license = "https://www.ebi.ac.uk/data-protection/privacy-notice/embl-ebi-public-website/";
        this.additionalProperties = additionalProperties;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDatePublished() {
        return datePublished;
    }

    public String getLicense() {
        return license;
    }

    public String getProjectAccession() {
        return projectAccession;
    }

    public List<Reference> getAdditionalProperties() {
        return additionalProperties;
    }
}
