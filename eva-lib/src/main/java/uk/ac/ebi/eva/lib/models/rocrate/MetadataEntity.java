package uk.ac.ebi.eva.lib.models.rocrate;

public class MetadataEntity extends RoCrateEntity {

    private Reference conformsTo;

    private Reference about;

    public MetadataEntity() {
        super("ro-crate-metadata.json", "CreativeWork");
        conformsTo = new Reference("https://w3id.org/ro/crate/1.2");
        about = new Reference("./");
    }

}
