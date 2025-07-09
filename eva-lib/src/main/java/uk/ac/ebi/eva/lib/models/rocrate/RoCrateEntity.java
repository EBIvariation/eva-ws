package uk.ac.ebi.eva.lib.models.rocrate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoCrateEntity {

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type;

    public RoCrateEntity() {}

    public RoCrateEntity(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

}
