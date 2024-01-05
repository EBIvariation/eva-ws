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
import uk.ac.ebi.eva.lib.eva_utils.EvaproDbUtils;

import javax.persistence.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Entity
@Table(name = "dgva_study_browser")
public class DgvaStudyBrowser {

    @Id
    @Column(name = "study_accession")
    private String studyAccession;

    @Column(name = "taxonomy_ids", length = 4000)
    private String taxId;

    @Column(name = "common_names", length = 4000)
    private String commonName;

    @Column(name = "scientific_names", length = 4000)
    private String scientificName;

    @Column(name = "pubmed_ids", length = 4000)
    private String pubmedId;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "study_type")
    private String studyType;

    @Column(name = "study_url")
    private String studyUrl;

    @Column(name = "study_description")
    private String studyDescription;

    @Column(name = "analysis_types", length = 4000)
    private String analysisType;

    @Column(name = "detection_methods", length = 4000)
    private String detectionMethod;

    @Column(name = "method_types", length = 4000)
    private String methodType;

    @Column(name = "platform_names", length = 4000)
    private String platformName;

    @Column(name = "assembly_names", length = 4000)
    private String assemblyName;

    @Column(name = "assembly_accessions", length = 4000)
    private String assemblyAccession;


    public DgvaStudyBrowser(String studyAccession, Integer callCount, Integer regionCount, Integer variantCount,
                            String taxId, String commonName, String scientificName, String pubmedId, String alias,
                            String displayName, String studyType, String projectId, String studyUrl,
                            String studyDescription, String analysisType, String detectionMethod,
                            String methodType, String platformName, String assemblyName, String assemblyAccession) {
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
        this.assemblyAccession = assemblyAccession;
    }

    DgvaStudyBrowser() { }

    public VariantStudy generateVariantStudy() {
        // Convert the list of tax ids to integer values
        int[] taxIds = Arrays.stream(taxId.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
        Arrays.sort(taxIds);

        // De-duplicate and concatenate fields. Couldn't be done using the Oracle-native listagg function due to error
        // "ORA-01489: result of string concatenation is too long"
        commonName = deduplicateAndSortAndJoin(commonName);
        scientificName = deduplicateAndSortAndJoin(scientificName);
        analysisType = deduplicateAndSortAndJoin(analysisType);
        platformName = deduplicateAndSortAndJoin(platformName);
        assemblyName = deduplicateAndSortAndJoin(assemblyName);
        assemblyAccession = deduplicateAndSortAndJoin(assemblyAccession);

        // Build the variant study object
        URI uri = null;
        String[] publications = null;
        try {
            uri = new URI(studyUrl);
        } catch (URISyntaxException | NullPointerException ex) {
            // Ignore, default value null.
        }
        publications = (pubmedId == null) ? null : pubmedId.split(", ");
        if (publications != null) {
            // Convert to CURIE to be consistent with EvaStudyBrowser
            for (int i = 0; i < publications.length; i++) {
                publications[i] = "PubMed:" + publications[i];
            }
        }

        VariantStudy study = new VariantStudy(displayName, studyAccession, null,
                                              studyDescription, taxIds, commonName, scientificName,
                                              null, null, null, null, EvaproDbUtils.stringToStudyType(studyType),
                                              analysisType, null, assemblyName, assemblyAccession, platformName, uri,
                                              publications, -1, -1, false);
        return study;
    }

    private String deduplicateAndSortAndJoin(String commaSeparatedValues) {
        if (commaSeparatedValues == null) {
            return commaSeparatedValues;
        }

        return Arrays.stream(commaSeparatedValues.split(",")).distinct().sorted().collect(Collectors.joining(", "));
    }
}
