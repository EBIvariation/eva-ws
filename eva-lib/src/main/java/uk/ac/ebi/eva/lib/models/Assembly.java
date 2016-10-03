/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014, 2015 EMBL - European Bioinformatics Institute
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

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class Assembly {
 
    private String assemblyAccession;
    private String assemblyChain;
    private String assemblyVersion;
    private String assemblyName;
    private String assemblyCode;
    
    private int taxonomyId;
    private String taxonomyCommonName;
    private String taxonomyScientificName;
    private String taxonomyCode;
    private String taxonomyEvaName;

    
    public Assembly(String assemblyAccession, String assemblyChain, String assemblyVersion, String assemblyName, String assemblyCode, 
            int taxonomyId, String taxonomyCommonName, String taxonomyScientificName, String taxonomyCode, String taxonomyEvaName) {
        this.assemblyAccession = assemblyAccession;
        this.assemblyChain = assemblyChain;
        this.assemblyVersion = assemblyVersion;
        this.assemblyName = assemblyName;
        this.assemblyCode = assemblyCode;
        this.taxonomyId = taxonomyId;
        this.taxonomyCommonName = taxonomyCommonName;
        this.taxonomyScientificName = taxonomyScientificName;
        this.taxonomyCode = taxonomyCode;
        this.taxonomyEvaName = taxonomyEvaName;
    }

    public String getAssemblyAccession() {
        return assemblyAccession;
    }

    public String getAssemblyChain() {
        return assemblyChain;
    }

    public String getAssemblyVersion() {
        return assemblyVersion;
    }

    public String getAssemblyName() {
        return assemblyName;
    }

    public String getAssemblyCode() {
        return assemblyCode;
    }

    public int getTaxonomyId() {
        return taxonomyId;
    }

    public String getTaxonomyCommonName() {
        return taxonomyCommonName;
    }

    public String getTaxonomyScientificName() {
        return taxonomyScientificName;
    }

    public String getTaxonomyCode() {
        return taxonomyCode;
    }

    public String getTaxonomyEvaName() {
        return taxonomyEvaName;
    }
    
    
}
