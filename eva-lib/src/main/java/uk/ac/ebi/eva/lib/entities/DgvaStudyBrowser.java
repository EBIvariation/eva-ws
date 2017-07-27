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

    @Column(name = "taxonomy_ids")
    private String taxId;

    @Column(name = "common_names")
    private String commonName;

    @Column(name = "scientific_names")
    private String scientificName;

    @Column(name = "pubmed_ids")
    private String pubmedId;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "study_type")
    private String studyType;

    @Column(name = "study_url")
    private String studyUrl;

    @Column(name = "study_description")
    private String studyDescription;

    @Column(length = 100, name = "analysis_types")
    private String analysisType;

    @Column(name = "detection_methods")
    private String detectionMethod;

    @Column(name = "method_types")
    private String methodType;

    @Column(name = "platform_names")
    private String platformName;

    @Column(name = "assembly_names")
    private String assemblyName;

    public DgvaStudyBrowser(String studyAccession, String taxId, String commonName, String scientificName,
                            String pubmedId, String displayName, String studyType, String projectId, String studyUrl,
                            String studyDescription, String analysisType, String detectionMethod, String methodType,
                            String platformName, String assemblyName) {
        this.studyAccession = studyAccession;
        this.taxId = taxId;
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.pubmedId = pubmedId;
        this.displayName = displayName;
        this.studyType = studyType;
        this.studyUrl = studyUrl;
        this.studyDescription = studyDescription;
        this.analysisType = analysisType;
        this.detectionMethod = detectionMethod;
        this.methodType = methodType;
        this.platformName = platformName;
        this.assemblyName = assemblyName;
    }

    DgvaStudyBrowser() { }

    public VariantStudy generateVariantStudy() {
        // Convert the list of tax ids to integer values
        int[] taxIds = Arrays.stream(taxId.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();

        // Build the variant study object
        URI uri = null;
        String[] publications = null;
        try {
            uri = new URI(studyUrl);
            publications = (pubmedId == null) ? null : pubmedId.split(",");
        } catch (URISyntaxException | NullPointerException ex) {
            // Ignore, default value null.
        }

        VariantStudy study = new VariantStudy(displayName, studyAccession, null,
                studyDescription, taxIds, commonName, scientificName,
                null, null, null, null, EvaproDbUtils.stringToStudyType(studyType), analysisType,
                null, assemblyName, platformName, uri, publications,
                                              -1);
        return study;
    }
}
