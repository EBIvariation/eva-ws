package uk.ac.ebi.eva.lib.models.rocrate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SampleEntity extends RoCrateEntity {

    private String name;

    @JsonProperty("identifier")
    private String sampleAccession;

    private String url;

    public SampleEntity() {
    }

    public SampleEntity(String fileName, String name, String sampleAccession) {
        super("#" + fileName + "-" + sampleAccession, "Sample");
        this.name = name;
        this.sampleAccession = sampleAccession;
        this.url = "https://www.ebi.ac.uk/biosamples/samples/" + sampleAccession;
    }

    public String getName() {
        return name;
    }

    public String getSampleAccession() {
        return sampleAccession;
    }
}
