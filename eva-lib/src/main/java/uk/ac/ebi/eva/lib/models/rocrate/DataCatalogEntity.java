package uk.ac.ebi.eva.lib.models.rocrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.time.LocalDate;
import java.util.List;

public class DataCatalogEntity extends RoCrateEntity {

    private static final String ID = "https://www.ebi.ac.uk/eva/";
    private static final String TYPE = "DataCatalog";
    private static final String IDENTIFIER = "EVA studies";
    private static final String LICENSE = "https://www.ebi.ac.uk/data-protection/privacy-notice/embl-ebi-public-website/";

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate datePublished;

    private String license;

    @JsonProperty("identifier")
    private String identifier;

    @JsonProperty("dataset")
    private List<Reference> projects;

    public DataCatalogEntity() {
    }

    public DataCatalogEntity(List<Reference> projects, LocalDate datePublished) {
        super(ID , TYPE);
        this.license = LICENSE;
        this.identifier = IDENTIFIER;
        this.projects = projects;
        this.datePublished = datePublished;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<Reference> getProjects() {
        return projects;
    }

    public LocalDate getDatePublished() {
        return datePublished;
    }

    public String getLicense() {
        return license;
    }

}
