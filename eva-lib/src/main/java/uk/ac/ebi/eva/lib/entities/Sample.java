package uk.ac.ebi.eva.lib.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "sample")
public class Sample {

    @Id
    @Column(name = "sample_id")
    private Long sampleId;

    @Column(name = "biosample_accession")
    private String biosampleAccession;

    @Column(name = "ena_accession")
    private String enaAccession;

    @OneToMany(mappedBy = "sample")
    private List<FileSample> fileSamples;

    public Sample() {
    }

    public Sample(Long sampleId, String biosampleAccession, String enaAccession) {
        this.sampleId = sampleId;
        this.biosampleAccession = biosampleAccession;
        this.enaAccession = enaAccession;
    }

    public String getBiosampleAccession() {
        return biosampleAccession;
    }

}
