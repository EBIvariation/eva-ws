package uk.ac.ebi.eva.server.ws.ga4gh;

public class BeaconDataset {
    private String id;
    private String name;
    private String description;
    private String assemblyId;
    private String createDateTime;
    private String updateDateTime;

    public BeaconDataset(String id, String name, String description, String assemblyId, String createDateTime, String updateDateTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.assemblyId = assemblyId;
        this.createDateTime = createDateTime;
        this.updateDateTime = updateDateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssemblyId() {
        return assemblyId;
    }

    public void setAssemblyId(String assemblyId) {
        this.assemblyId = assemblyId;
    }

    public String getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(String createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(String updateDateTime) {
        this.updateDateTime = updateDateTime;
    }
}
