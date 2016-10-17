package uk.ac.ebi.eva.lib.entity;


import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.utils.EvaproDbUtils;

import javax.persistence.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

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

    @Column(length = 100, name = "analysis_type")
    private String analysisType;

    @Column(name = "detection_method")
    private String detectionMethod;

    @Column(name = "method_type")
    private String methodType;

    @Column(name = "platform_name")
    private String platformName;

    @Column(name = "assembly_name")
    private String assemblyName;

    public VariantStudy generateVariantStudy() {
        // Convert the list of tax ids to integer values
        int[] taxIds = Arrays.stream(taxId.split(",")).mapToInt(Integer::parseInt).toArray();

        // Build the variant study object
        URI uri = null;
        String[] publications = null;
        try {
            uri = new URI(studyUrl);
            publications = (pubmedId == null) ? null : pubmedId.split(", ");
        } catch (URISyntaxException | NullPointerException ex) {
            // Ignore, default value null.
        }

        VariantStudy study = new VariantStudy(displayName, studyAccession, null,
                studyDescription, taxIds, commonName, scientificName,
                null, null, null, null, EvaproDbUtils.stringToStudyType(studyType), analysisType,
                null, assemblyName, platformName, uri, publications,
                variantCount, -1);
        return study;
    }
}
