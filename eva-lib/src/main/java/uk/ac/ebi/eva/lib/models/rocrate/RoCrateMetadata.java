package uk.ac.ebi.eva.lib.models.rocrate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RoCrateMetadata {

    @JsonProperty("@context")
    private String context;

    @JsonProperty("@graph")
    private List<RoCrateEntity> graph;

    public RoCrateMetadata(List<RoCrateEntity> graph) {
        this.context = "https://w3id.org/ro/crate/1.2/context";
        this.graph = graph;
    }

}
