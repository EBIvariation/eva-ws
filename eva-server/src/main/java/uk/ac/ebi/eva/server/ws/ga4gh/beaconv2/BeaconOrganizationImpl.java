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

package uk.ac.ebi.eva.server.ws.ga4gh.beaconv2;

import uk.ac.ebi.eva.commons.beacon.models.BeaconOrganization;
import uk.ac.ebi.eva.commons.beacon.models.KeyValuePair;

import java.util.List;
import java.util.Map;

public class BeaconOrganizationImpl extends BeaconOrganization {

    private static final String ID = "EMBL-EBI-EVA";

    private static final String NAME = "European Variation Archive (EMBL-EBI)";

    private static final String DESCRIPTION = "EMBL-EBI makes the world's public biological data freely available " +
            "to the scientific community via a range of services and tools, performs basic research and " +
            "provides professional training in bioinformatics. The European Variation Archive is an open-access" +
            " database of all types of genetic variation data from all species.";

    private static final String ADDRESS = "Wellcome Genome Campus, Hinxton, Cambridgeshire, CB10 1SD, United Kingdom";

    private static final String WELCOME_URL = "www.ebi.ac.uk/eva";

    private static final String CONTACT_URL = "www.ebi.ac.uk/eva/?Feedback";

    private static final String LOGO_URL = "www.ebi.ac.uk/eva/img/eva_logo.png";

    public BeaconOrganizationImpl() {
        setId(ID);
        setName(NAME);
        setDescription(DESCRIPTION);
        setAddress(ADDRESS);
        setWelcomeUrl(WELCOME_URL);
        setContactUrl(CONTACT_URL);
        setLogoUrl(LOGO_URL);
    }
}
