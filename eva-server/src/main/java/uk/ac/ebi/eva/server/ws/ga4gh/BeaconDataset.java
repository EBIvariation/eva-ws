/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.server.ws.ga4gh;

public class BeaconDataset {

    private String id;

    private String name;

    private String description;

    private String assemblyId;

    private String createDateTime;

    private String updateDateTime;

    private Integer sampleCount;

    private Integer variantCount;

    private Integer callCount;

    private String externalUrl;

    private String dataUsageConditions;

    public BeaconDataset(String id, String name, String description, String assemblyId, String createDateTime,
                         String updateDateTime, Integer sampleCount, Integer variantCount, Integer callCount,
                         String externalUrl, String dataUsageConditions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.assemblyId = assemblyId;
        this.createDateTime = createDateTime;
        this.updateDateTime = updateDateTime;
        this.sampleCount = sampleCount;
        this.variantCount = variantCount;
        this.callCount = callCount;
        this.externalUrl = externalUrl;
        this.dataUsageConditions = dataUsageConditions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssemblyId() {
        return assemblyId;
    }

    public void setAssemblyId(String assemblyId) {
        this.assemblyId = assemblyId;
    }

    public String getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(String createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(String updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }

    public Integer getVariantCount() {
        return variantCount;
    }

    public void setVariantCount(Integer variantCount) {
        this.variantCount = variantCount;
    }

    public Integer getCallCount() {
        return callCount;
    }

    public void setCallCount(Integer callCount) {
        this.callCount = callCount;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getDataUsageConditions() {
        return dataUsageConditions;
    }

    public void setDataUsageConditions(String dataUsageConditions) {
        this.dataUsageConditions = dataUsageConditions;
    }
}
