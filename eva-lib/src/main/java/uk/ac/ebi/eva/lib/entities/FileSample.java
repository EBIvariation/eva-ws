package uk.ac.ebi.eva.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "file_sample")
public class FileSample {

    @EmbeddedId
    private FileSamplePK fileSamplePK;

    @ManyToOne
    @JoinColumn(name = "file_id", insertable = false, updatable = false)
    private File file;

    @ManyToOne
    @JoinColumn(name = "sample_id", insertable = false, updatable = false)
    private Sample sample;

    @Column(name = "name_in_file")
    private String nameInFile;

    public FileSample() {
    }

    public FileSample(File file, Sample sample, String nameInFile) {
        this.file = file;
        this.sample = sample;
        this.nameInFile = nameInFile;
    }

    public FileSamplePK getFileSamplePK() {
        return fileSamplePK;
    }

    public void setFileSamplePK(FileSamplePK fileSamplePK) {
        this.fileSamplePK = fileSamplePK;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public String getNameInFile() {
        return nameInFile;
    }

    public void setNameInFile(String nameInFile) {
        this.nameInFile = nameInFile;
    }
}
