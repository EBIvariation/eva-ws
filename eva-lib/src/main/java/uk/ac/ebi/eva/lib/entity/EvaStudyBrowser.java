package uk.ac.ebi.eva.lib.entity;


import uk.ac.ebi.eva.lib.models.VariantStudy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Created by jorizci on 03/10/16.
 */
@Entity
@Table(name = "study_browser")
public class EvaStudyBrowser {

    @Id
    @Column(name = "projectAccession")
    private String projectAccession;

    @Column(name = "studyId")
    private long studyId;

    @Column(name = "projectTitle")
    private String projectTitle;

    private String description;

    @Column(name = "tax_iId")
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

    public VariantStudy generateVariantStudy() {
        // Convert the list of tax ids to integer values
        int[] taxIds = Arrays.stream(taxId.split(", ")).mapToInt(Integer::parseInt).toArray();

        // Build the variant study object
        URI uri = null;
        try {
            uri = new URI(resource);
        } catch (URISyntaxException | NullPointerException ex) {
            //Ignore, default values null
        }

        int notNullVariantCount = (variantCount == null) ? 0 : variantCount.intValue();

        return new VariantStudy(projectTitle, projectAccession, null,
                description, taxIds, commonName, scientificName,
                sourceType, center, material, scope,
                VariantStudy.StudyType.fromString(studyType), experimentType,
                experimentTypeAbbreviation, assemblyName, platform,
                uri, publications.split(", "), notNullVariantCount, samples);
    }
}
