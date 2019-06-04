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

import java.util.List;

public class GA4GHBeaconResponseV2 {

    public static String id_val = "uk.ac.ebi.eva";
    public static String name_val = "European Variation Archive Beacon";
    public static String apiVersion_val = "v1.0";
    public static String description_val = "descriptionString";
    public static String version_val = "v2";
    public static String welcomeUrl_val = "welcomeUrlString";
    public static String alternativeUrl_val = "alternativeUrlString";
    public static String createDateTime_val = "date1";
    public static String updateDateTime_val = "date2";

    private String id;
    private String name;
    private String apiVersion;
    private BeaconOrganization organization;
    private String description;
    private String version;
    private String welcomeUrl;
    private String alternativeUrl;
    private String createDateTime;
    private String updateDateTime;
    private List<BeaconDataset> beaconDatasetList;

    public GA4GHBeaconResponseV2() {
        this.id = id_val;
        this.name = name_val;
        this.apiVersion = apiVersion_val;
        this.organization = new BeaconOrganization();
        this.description = description_val;
        this.version = version_val;
        this.welcomeUrl = welcomeUrl_val;
        this.alternativeUrl = alternativeUrl_val;
        this.createDateTime = createDateTime_val;
        this.updateDateTime = updateDateTime_val;
    }

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
