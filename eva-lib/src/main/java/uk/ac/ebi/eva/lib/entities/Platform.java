package uk.ac.ebi.eva.lib.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "platform")
public class Platform {

    @Id
    @Column(name = "platform_id")
    private Long platformId;

    private String platform;

    private String manufacturer;

    public Platform() {
    }

    public Platform(Long platformId, String platform, String manufacturer) {
        this.platformId = platformId;
        this.platform = platform;
        this.manufacturer = manufacturer;
    }

    public String getPlatform() {
        return platform;
    }

}
