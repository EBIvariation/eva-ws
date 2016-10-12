package uk.ac.ebi.eva.lib.spring.data.entity;


import uk.ac.ebi.eva.lib.models.VariantStudy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by jorizci on 03/10/16.
 */
@Entity
@Table(name = "study_browser")
public class StudyBrowser {

    @Id
    @Column(name = "project_accession")
    private String projectAccession;

    @Column(name = "studyId")
    private long studyId;

    @Column(name = "projectTitle")
    private String projectTitle;

    private String description;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "common_name")
    private String commonName;

    @Column(name = "scientific_name")
    private String scientificName;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "study_type")
    private String studyType;

    @Column(name = "variant_count")
    private Long variantCount;

    private Integer samples;

    private String center;

    private String scope;

    private String material;

    private String publications;

    @Column(name = "associated_projects")
    private String associatedProjects;

    @Column(name = "experiment_type")
    private String experimentType;

    @Column(name = "experiment_type_abbreviation")
    private String experimentTypeAbbreviation;

    @Column(name = "assembly_accession")
    private String assemblyAccession;

    @Column(name = "assembly_name")
    private String assemblyName;

    private String platform;

    private String resource;

    public String getProjectAccession() {
        return projectAccession;
    }

    public void setProjectAccession(String projectAccession) {
        this.projectAccession = projectAccession;
    }

    public long getStudyId() {
        return studyId;
    }

    public void setStudyId(long studyId) {
        this.studyId = studyId;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getStudyType() {
        return studyType;
    }

    public void setStudyType(String studyType) {
        this.studyType = studyType;
    }

    public Long getVariantCount() {
        return variantCount;
    }

    public void setVariantCount(Long variantCount) {
        this.variantCount = variantCount;
    }

    public Integer getSamples() {
        return samples;
    }

    public void setSamples(Integer samples) {
        this.samples = samples;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
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

    public String getPublications() {
        return publications;
    }

    public void setPublications(String publications) {
        this.publications = publications;
    }

    public String getAssociatedProjects() {
        return associatedProjects;
    }

    public void setAssociatedProjects(String associatedProjects) {
        this.associatedProjects = associatedProjects;
    }

    public String getExperimentType() {
        return experimentType;
    }

    public void setExperimentType(String experimentType) {
        this.experimentType = experimentType;
    }

    public String getExperimentTypeAbbreviation() {
        return experimentTypeAbbreviation;
    }

    public void setExperimentTypeAbbreviation(String experimentTypeAbbreviation) {
        this.experimentTypeAbbreviation = experimentTypeAbbreviation;
    }

    public String getAssemblyAccession() {
        return assemblyAccession;
    }

    public void setAssemblyAccession(String assemblyAccession) {
        this.assemblyAccession = assemblyAccession;
    }

    public String getAssemblyName() {
        return assemblyName;
    }

    public void setAssemblyName(String assemblyName) {
        this.assemblyName = assemblyName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public VariantStudy generateVariantStudy() {
        // Convert the list of tax ids to integer values
        String[] taxIdStrings = getTaxId().split(", ");
        int[] taxIds = new int[taxIdStrings.length];
        for (int i = 0; i < taxIdStrings.length; i++) {
            taxIds[i] = Integer.parseInt(taxIdStrings[i]);
        }

        // Build the variant study object
        URI uri = null;
        try {
            uri = new URI(getResource());
        } catch (URISyntaxException | NullPointerException ex) {
            //Ignore, default values null
        }

        int variantCount = (getVariantCount() == null) ? 0 : getVariantCount().intValue();

        return new VariantStudy(getProjectTitle(), getProjectAccession(), null,
                getDescription(), taxIds, getCommonName(), getScientificName(),
                getSourceType(), getCenter(), getMaterial(), getScope(),
                VariantStudy.StudyType.fromString(getStudyType()), getExperimentType(),
                getExperimentTypeAbbreviation(), getAssemblyName(), getPlatform(),
                uri, getPublications().split(", "), variantCount, getSamples());
    }
}
