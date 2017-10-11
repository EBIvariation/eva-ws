/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2015 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.lib.models;

import uk.ac.ebi.eva.commons.core.models.StudyType;
import uk.ac.ebi.eva.commons.core.models.ws.VariantSourceEntryWithSampleNames;

import java.net.URI;
import java.util.List;

public class VariantStudy {

    private String name;

    private String id;

    private String description;

    private int[] taxonomyId;

    private String speciesCommonName;

    private String speciesScientificName;

    private String sourceType;

    private String center;

    private String material;

    private String scope;

    private StudyType type;

    private String experimentType;

    private String experimentTypeAbbreviation;

    private String assembly;

    private String platform;

    private URI url;

    private String[] publications;

    private int numVariants;

    private int numSamples;

    private List<VariantSourceEntryWithSampleNames> sources;

    private boolean browsable;

    public VariantStudy() {
        this(null, null);
    }

    public VariantStudy(String studyName, String studyId) {
        this(studyName, studyId, null);
    }

    public VariantStudy(String studyName, String studyId, List<VariantSourceEntryWithSampleNames> sources) {
        this(studyName, studyId, sources, null, new int[0], null, null, null, null, null, null, StudyType.COLLECTION,
             null, null, null, null, null, new String[0], -1, -1, false);
    }

    public VariantStudy(String studyName, String studyId, List<VariantSourceEntryWithSampleNames> sources,
                        String description, int[] taxonomyId, String speciesCommonName, String speciesScientificName,
                        String sourceType, String center, String material, String scope, StudyType type,
                        String experimentType, String experimentTypeAbbreviation, String referenceAssembly,
                        String platform, URI projectUrl, String[] publications, int numVariants, int numSamples,
                        boolean browsable) {

        this.name = studyName;
        this.id = studyId;
        this.description = description;
        this.taxonomyId = (taxonomyId == null) ? new int[0] : taxonomyId;
        this.speciesCommonName = speciesCommonName;
        this.speciesScientificName = speciesScientificName;
        this.sourceType = sourceType;
        this.center = center;
        this.material = material;
        this.scope = scope;
        this.type = type;
        this.experimentType = experimentType;
        this.experimentTypeAbbreviation = experimentTypeAbbreviation;
        this.assembly = referenceAssembly;
        this.platform = platform;
        this.url = projectUrl;
        this.publications = (publications == null) ? new String[0] : publications;
        this.numVariants = numVariants;
        this.numSamples = numSamples;
        this.sources = sources;
        this.browsable = browsable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int[] getTaxonomyId() {
        return taxonomyId;
    }

    public void setTaxonomyId(int[] taxonomyId) {
        this.taxonomyId = (taxonomyId == null) ? new int[0] : taxonomyId;
    }

    public String getSpeciesCommonName() {
        return speciesCommonName;
    }

    public void setSpeciesCommonName(String speciesCommonName) {
        this.speciesCommonName = speciesCommonName;
    }

    public String getSpeciesScientificName() {
        return speciesScientificName;
    }

    public void setSpeciesScientificName(String speciesScientificName) {
        this.speciesScientificName = speciesScientificName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public StudyType getType() {
        return type;
    }

    public void setType(StudyType type) {
        this.type = type;
    }

    public String getTypeName() {
        return type != null ? type.toString() : null;
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

    public String getAssembly() {
        return assembly;
    }

    public void setAssembly(String assembly) {
        this.assembly = assembly;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public int getNumVariants() {
        return numVariants;
    }

    public URI getUrl() {
        return url;
    }

    public void setUrl(URI url) {
        this.url = url;
    }

    public String[] getPublications() {
        return publications;
    }

    public void setPublications(String[] publications) {
        this.publications = (publications == null) ? new String[0] : publications;
    }

    public void setNumVariants(int numVariants) {
        this.numVariants = numVariants;
    }

    public int getNumSamples() {
        return numSamples;
    }

    public void setNumSamples(int numSamples) {
        this.numSamples = numSamples;
    }

    public List<VariantSourceEntryWithSampleNames> getSources() {
        return sources;
    }

    public void setSources(List<VariantSourceEntryWithSampleNames> sources) {
        this.sources = sources;
    }

    public boolean addSource(VariantSourceEntryWithSampleNames source) {
        return this.sources.add(source);
    }

    public boolean isBrowsable() {
        return browsable;
    }

    public void setBrowsable(boolean browsable) {
        this.browsable = browsable;
    }
}
