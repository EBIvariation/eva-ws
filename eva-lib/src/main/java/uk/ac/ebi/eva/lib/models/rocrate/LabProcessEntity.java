package uk.ac.ebi.eva.lib.models.rocrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.time.LocalDate;
import java.util.List;

public class LabProcessEntity extends RoCrateEntity {

    private String name;

    private String description;

    @JsonProperty("object")
    private List<Reference> samples;

    @JsonProperty("result")
    private List<Reference> files;

    @JsonProperty("endTime")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateSubmitted;

    @JsonProperty("identifier")
    private String analysisAccession;

    @JsonProperty("comment")
    // Analysis-level properties that aren't in bioschemas.org/LabProcess are included as Comments
    private List<Reference> additionalProperties;

    public LabProcessEntity() {
    }

    public LabProcessEntity(String analysisAccession, String name, String description, LocalDate dateSubmitted,
                            List<Reference> samples, List<Reference> files, List<Reference> additionalProperties) {
        // No analysis URL, so use the accession as the id
        super("#" + analysisAccession, "LabProcess");
        this.analysisAccession = analysisAccession;
        this.name = name;
        this.description = description;
        this.dateSubmitted = dateSubmitted;
        this.samples = samples;
        this.files = files;
        this.additionalProperties = additionalProperties;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Reference> getSamples() {
        return samples;
    }

    public List<Reference> getFiles() {
        return files;
    }

    public LocalDate getDateSubmitted() {
        return dateSubmitted;
    }

    public String getAnalysisAccession() {
        return analysisAccession;
    }

    public List<Reference> getAdditionalProperties() {
        return additionalProperties;
    }
}
