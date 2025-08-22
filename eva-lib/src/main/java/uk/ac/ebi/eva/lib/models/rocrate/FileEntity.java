package uk.ac.ebi.eva.lib.models.rocrate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FileEntity extends RoCrateEntity {

    private String name;

    private String description;

    @JsonProperty("encodingFormat")
    private String fileType;

    @JsonProperty("comment")
    private List<Reference> additionalProperties;

    public FileEntity() {
    }

    public FileEntity(String projectAccession, String name, String description, String fileType, List<Reference> additionalProperties) {
        super("https://ftp.ebi.ac.uk/pub/databases/eva/" + projectAccession + "/" + name, "File");
        this.name = name;
        this.description = description;
        this.fileType = fileType;
        this.additionalProperties = additionalProperties;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getFileType() {
        return fileType;
    }

    public List<Reference> getAdditionalProperties() {
        return additionalProperties;
    }
}
