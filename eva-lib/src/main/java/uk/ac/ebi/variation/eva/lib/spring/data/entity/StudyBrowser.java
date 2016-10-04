package uk.ac.ebi.variation.eva.lib.spring.data.entity;

import uk.ac.ebi.variation.eva.lib.models.VariantStudy;

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
    private String project_accession;

    private long study_id;

    private String project_title;

    private String description;

    private String tax_id;

    private String common_name;

    private String scientific_name;

    private String source_type;

    private String study_type;

    private Long variant_count;

    private Integer samples;

    private String center;

    private String scope;

    private String material;

    private String publications;

    private String associated_projects;

    private String experiment_type;

    private String experiment_type_abbreviation;

    private String assembly_accession;

    private String assembly_name;

    private String platform;

    private String resource;

    public String getProject_accession() {
        return project_accession;
    }

    public void setProject_accession(String project_accession) {
        this.project_accession = project_accession;
    }

    public long getStudy_id() {
        return study_id;
    }

    public void setStudy_id(long study_id) {
        this.study_id = study_id;
    }

    public String getProject_title() {
        return project_title;
    }

    public void setProject_title(String project_title) {
        this.project_title = project_title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getSource_type() {
        return source_type;
    }

    public void setSource_type(String source_type) {
        this.source_type = source_type;
    }

    public String getStudy_type() {
        return study_type;
    }

    public void setStudy_type(String study_type) {
        this.study_type = study_type;
    }

    public Long getVariant_count() {
        return variant_count;
    }

    public void setVariant_count(Long variant_count) {
        this.variant_count = variant_count;
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

    public String getAssociated_projects() {
        return associated_projects;
    }

    public void setAssociated_projects(String associated_projects) {
        this.associated_projects = associated_projects;
    }

    public String getExperiment_type() {
        return experiment_type;
    }

    public void setExperiment_type(String experiment_type) {
        this.experiment_type = experiment_type;
    }

    public String getExperiment_type_abbreviation() {
        return experiment_type_abbreviation;
    }

    public void setExperiment_type_abbreviation(String experiment_type_abbreviation) {
        this.experiment_type_abbreviation = experiment_type_abbreviation;
    }

    public String getAssembly_accession() {
        return assembly_accession;
    }

    public void setAssembly_accession(String assembly_accession) {
        this.assembly_accession = assembly_accession;
    }

    public String getAssembly_name() {
        return assembly_name;
    }

    public void setAssembly_name(String assembly_name) {
        this.assembly_name = assembly_name;
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
        String[] taxIdStrings = getTax_id().split(", ");
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
        return new VariantStudy(getProject_title(), getProject_accession(), null,
                getDescription(), taxIds, getCommon_name(), getScientific_name(),
                getSource_type(), getCenter(), getMaterial(), getScope(),
                VariantStudy.StudyType.fromString(getStudy_type()), getExperiment_type(),
                getExperiment_type_abbreviation(), getAssembly_name(), getPlatform(),
                uri, getPublications().split(", "), getVariant_count().intValue(), getSamples());
    }
}
