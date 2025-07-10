/*
 * Copyright 2014-2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.lib.entities;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

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

    @ManyToMany
    @JoinTable(name="project_taxonomy",
            joinColumns = @JoinColumn(name="project_accession"),
            inverseJoinColumns = @JoinColumn(name="taxonomy_id"))
    private List<Taxonomy> taxonomies;

    @OneToMany
    @JoinTable(name="project_dbxref",
            joinColumns = @JoinColumn(name="project_accession"),
            inverseJoinColumns = @JoinColumn(name="dbxref_id"))
    private List<DbXref> dbXrefs;

    @OneToMany
    @JoinTable(name="project_ena_submission",
            joinColumns = @JoinColumn(name="project_accession"),
            inverseJoinColumns = @JoinColumn(name="submission_id"))
    private List<Submission> submissions;

    public Project() {}

    public Project(String projectAccession, String centerName, String alias, String title, String description,
                   String scope, String material, String selection, String type, String secondaryStudyId,
                   String sourceType, Long projectAccessionCode, String evaDescription,
                   String evaCenterName, String evaSubmitterLink, Long evaStudyAccession, String studyType) {
        this.projectAccession = projectAccession;
        this.centerName = centerName;
        this.alias = alias;
        this.title = title;
        this.description = description;
        this.scope = scope;
        this.material = material;
        this.selection = selection;
        this.type = type;
        this.secondaryStudyId = secondaryStudyId;
        this.sourceType = sourceType;
        this.projectAccessionCode = projectAccessionCode;
        this.evaDescription = evaDescription;
        this.evaCenterName = evaCenterName;
        this.evaSubmitterLink = evaSubmitterLink;
        this.evaStudyAccession = evaStudyAccession;
        this.studyType = studyType;
    }


    public String getProjectAccession() {
        return projectAccession;
    }

    public String getCenterName() {
        return centerName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getScope() {
        return scope;
    }

    public String getMaterial() {
        return material;
    }

    public String getSourceType() {
        return sourceType;
    }

    public List<Taxonomy> getTaxonomies() {
        return taxonomies;
    }

    public void setTaxonomies(List<Taxonomy> taxonomies) {
        this.taxonomies = taxonomies;
    }

    public List<DbXref> getDbXrefs() {
        return dbXrefs;
    }

    public void setDbXrefs(List<DbXref> dbXrefs) {
        this.dbXrefs = dbXrefs;
    }

    public List<Submission> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<Submission> submissions) {
        this.submissions = submissions;
    }

}
