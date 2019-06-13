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

import uk.ac.ebi.eva.commons.beacon.models.BeaconOrganization;

import java.util.Map;

public class BeaconOrganizationImpl implements BeaconOrganization {

    private static final String ID = "EMBL-EBI-EVA";

    private static final String NAME = "European Variation Archive (EMBL-EBI)";

    private static final String DESCRIPTION = "EMBL-EBI makes the world's public biological data freely available " +
            "to the scientific community via a range of services and tools, performs basic research and " +
            "provides professional training in bioinformatics. The European Variation Archive is an open-access" +
            " database of all types of genetic variation data from all species.";

    private static final String ADDRESS = "Wellcome Genome Campus, Hinxton, Cambridgeshire, CB10 1SD, United Kingdom";

    private static final String WELCOME_URL = "www.ebi.ac.uk/eva";

    private static final String CONTACT_URL = "contactUrlString";

    private static final String LOGO_URL = "www.ebi.ac.uk/eva/img/eva_logo.png";

    private static  Map<String, String> INFO = null;

    private String id;

    private String name;

    private String description;

    private String address;

    private String welcomeUrl;

    private String contactUrl;

    private String logoUrl;

    private Map<String, String> info;

    public BeaconOrganizationImpl() {
        this.id = ID;
        this.name = NAME;
        this.description = DESCRIPTION;
        this.address = ADDRESS;
        this.welcomeUrl = WELCOME_URL;
        this.contactUrl = CONTACT_URL;
        this.logoUrl = LOGO_URL;
        this.info = INFO;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public String getWelcomeUrl() {
        return welcomeUrl;
    }

    @Override
    public String getContactUrl() {
        return contactUrl;
    }

    @Override
    public String getLogoUrl() {
        return logoUrl;
    }

    @Override
    public Map<String, String> getInfo() {
        return info;
    }
}
