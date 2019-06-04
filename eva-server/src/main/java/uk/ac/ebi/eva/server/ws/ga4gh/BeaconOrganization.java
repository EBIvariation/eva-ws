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
    public static   String id_val = "EMBL-EBI-EVA";
    public static   String name_val = "European Variation Archive (EMBL-EBI)";
    public static   String description_val = "EMBL-EBI makes the world's public biological data freely available to the " +
            "scientific community via a range of services and tools, performs basic research and provides " +
            "professional training in bioinformatics.The European Variation Archive is an open-access database of " +
            "all types of genetic variation data from all species.";
    public static  String address_val="Wellcome Genome Campus, Hinxton, Cambridgeshire, CB10 1SD, United Kingdom";
    public static   String welcomeUrl_val="www.ebi.ac.uk/eva";
    public static   String contactUrl_val="contactUrlString";
    public static   String logoUrl_val="www.ebi.ac.uk/eva/img/eva_logo.png";
    public static   Map<String,String> info_val=null;

    private  String id;
    private  String name;
    private  String description;
    private  String address;
    private  String welcomeUrl;
    private  String contactUrl;
    private  String logoUrl;
    private  Map<String,String> info;

    public BeaconOrganization() {
        this.id = id_val;
        this.name = name_val;
        this.description = description_val;
        this.address = address_val;
        this.welcomeUrl = welcomeUrl_val;
        this.contactUrl = contactUrl_val;
        this.logoUrl = logoUrl_val;
        this.info = info_val;
    }

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
