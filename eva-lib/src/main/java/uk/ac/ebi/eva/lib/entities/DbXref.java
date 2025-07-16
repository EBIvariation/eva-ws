package uk.ac.ebi.eva.lib.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dbxref")
public class DbXref {

    @Id
    @Column(name = "dbxref_id")
    private Long dbxrefId;

    private String db;

    private String id;

    private String label;

    @Column(name = "link_type")
    private String linkType;

    @Column(name = "source_object")
    private String sourceObject;

    public DbXref() {}

    public DbXref(Long dbxrefId, String db, String id, String label, String linkType, String sourceObject) {
        this.dbxrefId = dbxrefId;
        this.db = db;
        this.id = id;
        this.label = label;
        this.linkType = linkType;
        this.sourceObject = sourceObject;
    }

    public String getCurie() {
        return db + ":" + id;
    }

    public String getLinkType() {
        return linkType;
    }
}
