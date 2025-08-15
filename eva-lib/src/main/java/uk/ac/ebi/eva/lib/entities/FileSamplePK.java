package uk.ac.ebi.eva.lib.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class FileSamplePK implements Serializable {

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "sample_id")
    private Long sampleId;

}
