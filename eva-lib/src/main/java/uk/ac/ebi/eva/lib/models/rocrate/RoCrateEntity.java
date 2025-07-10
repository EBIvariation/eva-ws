package uk.ac.ebi.eva.lib.models.rocrate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

// Use schema.org type to deserialize JSON into appropriate subtype
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "@type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CommentEntity.class, name = "Comment"),
        @JsonSubTypes.Type(value = DatasetEntity.class, name = "Dataset"),
        @JsonSubTypes.Type(value = MetadataEntity.class, name = "CreativeWork")
})
public abstract class RoCrateEntity {

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
