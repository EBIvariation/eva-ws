package uk.ac.ebi.eva.lib.spring.data.entity;


import uk.ac.ebi.eva.lib.datastore.EvaproUtils;
import uk.ac.ebi.eva.lib.models.VariantStudy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by jorizci on 28/09/16.
 */
@Entity
@Table(name = "dgva_study_browser")
public class DgvaStudyBrowser {

    @Id
    @Column(name = "study_accession")
    private String studyAccession;

    @Column(name = "call_count")
    private Integer callCount;

    @Column(name = "region_count")
    private Integer regionCount;

    @Column(name = "variant_count")
    private Integer variantCount;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "common_name")
    private String commonName;

    @Column(name = "scientific_name")
    private String scientificName;

    @Column(name = "pubmed_id")
    private String pubmedId;

    private String alias;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "study_type")
    private String studyType;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "study_url")
    private String studyUrl;

    @Column(name = "study_description")
    private String studyDescription;

    @Column(name = "analysis_type")
    private String analysisType;

    @Column(name = "detection_method")
    private String detectionMethod;

    @Column(name = "method_type")
    private String methodType;

    @Column(name = "platform_name")
    private String platformName;

    @Column(name = "assembly_name")
    private String assemblyName;

    public String getStudyAccession() {
        return studyAccession;
    }

    public void setStudyAccession(String studyAccession) {
        this.studyAccession = studyAccession;
    }

    public Integer getCallCount() {
        return callCount;
    }

    public void setCallCount(Integer callCount) {
        this.callCount = callCount;
    }

    public Integer getRegionCount() {
        return regionCount;
    }

    public void setRegionCount(Integer regionCount) {
        this.regionCount = regionCount;
    }

    public Integer getVariantCount() {
        return variantCount;
    }

    public void setVariantCount(Integer variantCount) {
        this.variantCount = variantCount;
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

    public String getPubmedId() {
        return pubmedId;
    }

    public void setPubmedId(String pubmedId) {
        this.pubmedId = pubmedId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getStudyType() {
        return studyType;
    }

    public void setStudyType(String studyType) {
        this.studyType = studyType;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getStudyUrl() {
        return studyUrl;
    }

    public void setStudyUrl(String studyUrl) {
        this.studyUrl = studyUrl;
    }

    public String getStudyDescription() {
        return studyDescription;
    }

    public void setStudyDescription(String studyDescription) {
        this.studyDescription = studyDescription;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public String getDetectionMethod() {
        return detectionMethod;
    }

    public void setDetectionMethod(String detectionMethod) {
        this.detectionMethod = detectionMethod;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getAssemblyName() {
        return assemblyName;
    }

    public void setAssemblyName(String assemblyName) {
        this.assemblyName = assemblyName;
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
        String[] publications = null;
        try {
            uri = new URI(getStudyUrl());
            String pubmedIds = getPubmedId();
            publications = (pubmedIds == null) ? null : pubmedIds.split(", ");
        } catch (URISyntaxException | NullPointerException ex) {
            // Ignore, default value null.
        }

        VariantStudy study = new VariantStudy(getDisplayName(), getStudyAccession(), null,
                getStudyDescription(), taxIds, getCommonName(), getScientificName(),
                null, null, null, null, EvaproUtils.stringToStudyType(getStudyType()), getAnalysisType(),
                null, getAssemblyName(), getPlatformName(), uri, publications,
                getVariantCount(), -1);
        return study;
    }
}
