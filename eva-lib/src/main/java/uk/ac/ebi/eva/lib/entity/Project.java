package uk.ac.ebi.eva.lib.entity;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Created by jorizci on 03/10/16.
 */
@Entity
@Table(name = "project")
public class Project {

    @Id
    @Column(length = 45, nullable = false, name = "project_accession")
    private String projectAccession;

    @Column(length = 250, nullable = false, name= "center_name")
    private String centerName;

    @Column(length = 4000, nullable = false)
    private String alias;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String title;

    @Column(length = 16000)
    private String description;

    @Column(length = 45, nullable = false)
    private String scope;

    @Column(length = 45, nullable = false)
    private String material;

    @Column(length = 45, nullable = true)
    private String selection = "other";

    @Column(length = 45, nullable = false)
    private String type = "Umbrella";

    @Column(length = 45, nullable = false, name = "secondary_study_id")
    private String secondaryStudyId;

    @Column(name = "hold_date")
    private Date holdDate;

    @Column(length = 10, name = "source_type", nullable = false)
    private String sourceType = "Germline";

    @Column(name = "project_accession_code", unique = true)
    private Long projectAccessionCode;

    @Column(length = 4000, name="eva_description")
    private String evaDescription;

    @Column(length = 4000, name="eva_center_name")
    private String evaCenterName;

    @Column(length = 4000, name="eva_submitter_link")
    private String evaSubmitterLink;

    @Column(name ="eva_study_accession")
    private Long evaStudyAccession;

    @Column(name = "ena_status")
    private Integer enaStatus = 4;

    @Column(name = "eva_status")
    private Integer evaStatus = 1;

    @Column(name = "ena_timestamp")
    private Timestamp enaTimestamp;

    @Column(name = "eva_timestamp")
    private Timestamp evaTimestamp;

    @Column(length = 100, name= "study_type")
    private String studyType;

}
