package uk.ac.ebi.eva.lib.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FileSamplePK implements Serializable {

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "sample_id")
    private Long sampleId;

    public FileSamplePK() {
    }

    public FileSamplePK(Long fileId, Long sampleId) {
        this.fileId = fileId;
        this.sampleId = sampleId;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileSamplePK other = (FileSamplePK) o;
        return fileId.equals(other.getFileId()) && sampleId.equals(other.getSampleId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, sampleId);
    }

}
