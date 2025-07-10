package uk.ac.ebi.eva.lib.models.rocrate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Reference {

    @JsonProperty("@id")
    private String id;

    public Reference() {}

    public Reference(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
