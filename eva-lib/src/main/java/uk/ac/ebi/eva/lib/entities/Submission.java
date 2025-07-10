package uk.ac.ebi.eva.lib.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "submission")
public class Submission {

    @Id
    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "submission_accession")
    private String submissionAccession;

    private String type;

    private String action;

    private String title;

    private String notes;

    private LocalDate date;

    private Integer brokered;

    public Submission() {
    }

    public Submission(Long submissionId, String submissionAccession, String type, String action, String title,
                      String notes, LocalDate date, Integer brokered) {
        this.submissionId = submissionId;
        this.submissionAccession = submissionAccession;
        this.type = type;
        this.action = action;
        this.title = title;
        this.notes = notes;
        this.date = date;
        this.brokered = brokered;
    }

    public LocalDate getDate() {
        return date;
    }
}
