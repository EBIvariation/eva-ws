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


import uk.ac.ebi.eva.commons.core.models.StudyType;
import uk.ac.ebi.eva.lib.models.VariantStudy;

import javax.persistence.*;
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
    @Column(length = 45, name = "project_accession")
    private String projectAccession;

    @Column(name = "study_id")
    private long studyId;

    @Column(name = "project_title")
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

    public EvaStudyBrowser(String projectAccession, long studyId, String projectTitle, String description,
                           String taxId, String commonName, String scientificName, String sourceType,
                           String studyType, Long variantCount, Integer samples, String center, String scope,
                           String material, String publications, String associatedProjects, String experimentType,
                           String experimentTypeAbbreviation, String assemblyAccession, String assemblyName,
                           String platform, String resource) {
        this.projectAccession = projectAccession;
        this.studyId = studyId;
        this.projectTitle = projectTitle;
        this.description = description;
        this.taxId = taxId;
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.sourceType = sourceType;
        this.studyType = studyType;
        this.variantCount = variantCount;
        this.samples = samples;
        this.center = center;
        this.scope = scope;
        this.material = material;
        this.publications = publications;
        this.associatedProjects = associatedProjects;
        this.experimentType = experimentType;
        this.experimentTypeAbbreviation = experimentTypeAbbreviation;
        this.assemblyAccession = assemblyAccession;
        this.assemblyName = assemblyName;
        this.platform = platform;
        this.resource = resource;
    }

    EvaStudyBrowser() { }

    public VariantStudy generateVariantStudy() {
        // Convert the list of tax ids to integer values
        int[] taxIds = Arrays.stream(taxId.split(", ")).map(String::trim).mapToInt(Integer::parseInt).toArray();

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
                StudyType.fromString(studyType), experimentType,
                experimentTypeAbbreviation, assemblyName, platform,
                uri, publications.split(", "), notNullVariantCount, samples);
    }
}
