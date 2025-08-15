package uk.ac.ebi.eva.lib.models.rocrate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoCrateMetadata {

    @JsonProperty("@context")
    private String context;

    @JsonProperty("@graph")
    private List<RoCrateEntity> graph;

    public RoCrateMetadata() {
    }

    public RoCrateMetadata(List<RoCrateEntity> graph) {
        this.context = "https://w3id.org/ro/crate/1.2/context";
        this.graph = graph;
        if (this.graph == null) {
            this.graph = new ArrayList<>();
        }
        // Add the entity that describes this metadata document
        this.graph.add(0, new MetadataEntity());
    }

    public String getContext() {
        return context;
    }

    public List<RoCrateEntity> getGraph() {
        return graph;
    }

    public List<RoCrateEntity> getEntities(List<Reference> references) {
        List<String> identifiers = references.stream().map(Reference::getId).collect(Collectors.toList());
        return graph.stream().filter(roCrateEntity -> identifiers.contains(roCrateEntity.getId()))
                    .collect(Collectors.toList());
    }

}
