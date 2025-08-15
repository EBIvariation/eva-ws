package uk.ac.ebi.eva.lib.models.rocrate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SampleEntity extends RoCrateEntity {

    private String name;

    @JsonProperty("identifier")
    private String sampleAccession;

    public SampleEntity() {
    }

    public SampleEntity(String name, String sampleAccession) {
        super("https://www.ebi.ac.uk/biosamples/samples/" + sampleAccession, "Sample");
        this.name = name;
        this.sampleAccession = sampleAccession;
    }

    public String getName() {
        return name;
    }

    public String getSampleAccession() {
        return sampleAccession;
    }
}
