package uk.ac.ebi.eva.lib.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Date;
import java.util.List;

@Entity
@Table(name = "analysis")
public class Analysis {

    @Id
    @Column(name = "analysis_accession")
    private String analysisAccession;

    private String title;

    private String alias;

    private String description;

    @Column(name = "center_name")
    private String centerName;

    private Date date;

    @Column(name = "vcf_reference")
    private String vcfReference;

    @Column(name = "vcf_reference_accession")
    private String vcfReferenceAccession;

    @Column(name = "hidden_in_eva")
    private int hiddenInEva;

    @Column(name = "assembly_set_id")
    private int assemblySetId;

    @OneToMany
    @JoinTable(name = "analysis_file",
            joinColumns = @JoinColumn(name = "analysis_accession"),
            inverseJoinColumns = @JoinColumn(name = "file_id"))
    private List<File> files;

    @OneToOne
    @JoinTable(name = "analysis_submission",
            joinColumns = @JoinColumn(name = "analysis_accession"),
            inverseJoinColumns = @JoinColumn(name = "submission_id"))
    private Submission submission;

    @ManyToOne
    @JoinTable(name = "analysis_experiment_type",
            joinColumns = @JoinColumn(name = "analysis_accession"),
            inverseJoinColumns = @JoinColumn(name = "experiment_type_id"))
    private ExperimentType experimentType;

    @ManyToOne
    @JoinTable(name = "analysis_platform",
            joinColumns = @JoinColumn(name = "analysis_accession"),
            inverseJoinColumns = @JoinColumn(name = "platform_id"))
    private Platform platform;

    public Analysis() {
    }

    public Analysis(String analysisAccession, String title, String alias, String description, String centerName,
                    Date date, String vcfReference, String vcfReferenceAccession, int hiddenInEva, int assemblySetId) {
        this.analysisAccession = analysisAccession;
        this.title = title;
        this.alias = alias;
        this.description = description;
        this.centerName = centerName;
        this.date = date;
        this.vcfReference = vcfReference;
        this.vcfReferenceAccession = vcfReferenceAccession;
        this.hiddenInEva = hiddenInEva;
        this.assemblySetId = assemblySetId;
    }

    public String getAnalysisAccession() {
        return analysisAccession;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    public String getVcfReferenceAccession() {
        return vcfReferenceAccession;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public ExperimentType getExperimentType() {
        return experimentType;
    }

    public void setExperimentType(ExperimentType experimentType) {
        this.experimentType = experimentType;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }
}
