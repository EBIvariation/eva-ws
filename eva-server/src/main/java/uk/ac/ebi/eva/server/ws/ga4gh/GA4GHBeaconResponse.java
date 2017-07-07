/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.server.ws.ga4gh;

public class GA4GHBeaconResponse {

    private String chromosome;

    private Integer start;

    private String allele;

    private String datasetIds;

    private boolean exists;

    private String errorMessage;

    GA4GHBeaconResponse() {
    }

    public GA4GHBeaconResponse(String chromosome, Integer start, String allele, String datasetIds, boolean exists) {
        this.chromosome = chromosome;
        this.start = start;
        this.allele = allele;
        this.datasetIds = datasetIds;
        this.exists = exists;
    }

    public GA4GHBeaconResponse(String chromosome, Integer start, String allele, String datasetIds, String errorMessage) {
        this.chromosome = chromosome;
        this.start = start;
        this.allele = allele;
        this.datasetIds = datasetIds;
        this.errorMessage = errorMessage;
    }

    public boolean isExists() {
        return exists;
    }
}
