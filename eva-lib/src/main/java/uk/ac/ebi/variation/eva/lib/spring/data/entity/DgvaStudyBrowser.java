package uk.ac.ebi.variation.eva.lib.spring.data.entity;

import uk.ac.ebi.variation.eva.lib.datastore.EvaproUtils;
import uk.ac.ebi.variation.eva.lib.models.VariantStudy;

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
    private String study_accession;

    private Integer call_count;
    private Integer region_count;
    private Integer variant_count;
    private String tax_id;
    private String common_name;
    private String scientific_name;
    private String pubmed_id;
    private String alias;
    private String display_name;
    private String study_type;
    private String project_id;
    private String study_url;
    private String study_description;
    private String analysis_type;
    private String detection_method;
    private String method_type;
    private String platform_name;
    private String assembly_name;

    public String getStudy_accession() {
        return study_accession;
    }

    public void setStudy_accession(String study_accession) {
        this.study_accession = study_accession;
    }

    public Integer getCall_count() {
        return call_count;
    }

    public void setCall_count(Integer call_count) {
        this.call_count = call_count;
    }

    public Integer getRegion_count() {
        return region_count;
    }

    public void setRegion_count(Integer region_count) {
        this.region_count = region_count;
    }

    public Integer getVariant_count() {
        return variant_count;
    }

    public void setVariant_count(Integer variant_count) {
        this.variant_count = variant_count;
    }

    public String getTax_id() {
        return tax_id;
    }

    public void setTax_id(String tax_id) {
        this.tax_id = tax_id;
    }

    public String getCommon_name() {
        return common_name;
    }

    public void setCommon_name(String common_name) {
        this.common_name = common_name;
    }

    public String getScientific_name() {
        return scientific_name;
    }

    public void setScientific_name(String scientific_name) {
        this.scientific_name = scientific_name;
    }

    public String getPubmed_id() {
        return pubmed_id;
    }

    public void setPubmed_id(String pubmed_id) {
        this.pubmed_id = pubmed_id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getStudy_type() {
        return study_type;
    }

    public void setStudy_type(String study_type) {
        this.study_type = study_type;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getStudy_url() {
        return study_url;
    }

    public void setStudy_url(String study_url) {
        this.study_url = study_url;
    }

    public String getStudy_description() {
        return study_description;
    }

    public void setStudy_description(String study_description) {
        this.study_description = study_description;
    }

    public String getAnalysis_type() {
        return analysis_type;
    }

    public void setAnalysis_type(String analysis_type) {
        this.analysis_type = analysis_type;
    }

    public String getDetection_method() {
        return detection_method;
    }

    public void setDetection_method(String detection_method) {
        this.detection_method = detection_method;
    }

    public String getMethod_type() {
        return method_type;
    }

    public void setMethod_type(String method_type) {
        this.method_type = method_type;
    }

    public String getPlatform_name() {
        return platform_name;
    }

    public void setPlatform_name(String platform_name) {
        this.platform_name = platform_name;
    }

    public String getAssembly_name() {
        return assembly_name;
    }

    public void setAssembly_name(String assembly_name) {
        this.assembly_name = assembly_name;
    }

    public VariantStudy generateVariantStudy(){
        // Convert the list of tax ids to integer values
        String[] taxIdStrings = getTax_id().split(", ");
        int[] taxIds = new int[taxIdStrings.length];
        for (int i = 0; i < taxIdStrings.length; i++) {
            taxIds[i] = Integer.parseInt(taxIdStrings[i]);
        }

        // Build the variant study object
        URI uri = null;
        String[] publications = null;
        try {
            uri = new URI(getStudy_url());
            String pubmedIds = getPubmed_id();
            publications = (pubmedIds == null) ? null : pubmedIds.split(", ");
        } catch (URISyntaxException | NullPointerException ex) {
            // Ignore, default value null.
        }

        VariantStudy study = new VariantStudy(getDisplay_name(), getStudy_accession(), null,
                getStudy_description(), taxIds, getCommon_name(), getScientific_name(),
                null, null, null, null, EvaproUtils.stringToStudyType(getStudy_type()), getAnalysis_type(),
                null, getAssembly_name(), getPlatform_name(), uri, publications,
                getVariant_count(), -1);
        return study;
    }
}
