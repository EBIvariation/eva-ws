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

public class GA4GHBeaconResponseV2 {

    private  String id = "uk.ac.ebi.eva";
    private  String name = "European Variation Archive Beacon";
    private  String apiVersion = "v1.0";
    private  BeaconOrganization organization = new BeaconOrganization();
    private  String description = "descriptionString";
    private  String version = "v2";
    private  String welcomeUrl = "welcomeUrlString";
    private  String alternativeUrl = "alternativeUrlString";
    private  String createDateTime = "date1";
    private  String updateDateTime = "date2";

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public BeaconOrganization getOrganization() {
        return organization;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public String getWelcomeUrl() {
        return welcomeUrl;
    }

    public String getAlternativeUrl() {
        return alternativeUrl;
    }

    public String getCreateDateTime() {
        return createDateTime;
    }

    public String getUpdateDateTime() {
        return updateDateTime;
    }
}
