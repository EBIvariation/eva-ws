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

import java.util.Map;

public class BeaconOrganization {
    private  String id = "EMBL-EBI-EVA";
    private  String name = "European Variation Archive (EMBL-EBI)";
    private  String description = "EMBL-EBI makes the world's public biological data freely available to the " +
            "scientific community via a range of services and tools, performs basic research and provides " +
            "professional training in bioinformatics.The European Variation Archive is an open-access database of " +
            "all types of genetic variation data from all species.";
    private  String address="Wellcome Genome Campus, Hinxton, Cambridgeshire, CB10 1SD, United Kingdom";
    private  String welcomeUrl="www.ebi.ac.uk/eva";
    private  String contactUrl="contactUrlString";
    private  String logoUrl="www.ebi.ac.uk/eva/img/eva_logo.png";
    private  Map<String,String> info=null;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getWelcomeUrl() {
        return welcomeUrl;
    }

    public String getContactUrl() {
        return contactUrl;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public Map<String, String> getInfo() {
        return info;
    }
}
