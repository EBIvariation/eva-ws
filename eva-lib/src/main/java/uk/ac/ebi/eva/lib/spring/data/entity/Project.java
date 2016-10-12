package uk.ac.ebi.eva.lib.spring.data.entity;

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

    public String getProjectAccession() {
        return projectAccession;
    }

    public void setProjectAccession(String projectAccession) {
        this.projectAccession = projectAccession;
    }

    public String getCenterName() {
        return centerName;
    }

    public void setCenterName(String centerName) {
        this.centerName = centerName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSecondaryStudyId() {
        return secondaryStudyId;
    }

    public void setSecondaryStudyId(String secondaryStudyId) {
        this.secondaryStudyId = secondaryStudyId;
    }

    public Date getHoldDate() {
        return holdDate;
    }

    public void setHoldDate(Date holdDate) {
        this.holdDate = holdDate;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getProjectAccessionCode() {
        return projectAccessionCode;
    }

    public void setProjectAccessionCode(Long projectAccessionCode) {
        this.projectAccessionCode = projectAccessionCode;
    }

    public String getEvaDescription() {
        return evaDescription;
    }

    public void setEvaDescription(String evaDescription) {
        this.evaDescription = evaDescription;
    }

    public String getEvaCenterName() {
        return evaCenterName;
    }

    public void setEvaCenterName(String evaCenterName) {
        this.evaCenterName = evaCenterName;
    }

    public String getEvaSubmitterLink() {
        return evaSubmitterLink;
    }

    public void setEvaSubmitterLink(String evaSubmitterLink) {
        this.evaSubmitterLink = evaSubmitterLink;
    }

    public Long getEvaStudyAccession() {
        return evaStudyAccession;
    }

    public void setEvaStudyAccession(Long evaStudyAccession) {
        this.evaStudyAccession = evaStudyAccession;
    }

    public Integer getEnaStatus() {
        return enaStatus;
    }

    public void setEnaStatus(Integer enaStatus) {
        this.enaStatus = enaStatus;
    }

    public Integer getEvaStatus() {
        return evaStatus;
    }

    public void setEvaStatus(Integer evaStatus) {
        this.evaStatus = evaStatus;
    }

    public Timestamp getEnaTimestamp() {
        return enaTimestamp;
    }

    public void setEnaTimestamp(Timestamp enaTimestamp) {
        this.enaTimestamp = enaTimestamp;
    }

    public Timestamp getEvaTimestamp() {
        return evaTimestamp;
    }

    public void setEvaTimestamp(Timestamp evaTimestamp) {
        this.evaTimestamp = evaTimestamp;
    }

    public String getStudyType() {
        return studyType;
    }

    public void setStudyType(String studyType) {
        this.studyType = studyType;
    }
}
