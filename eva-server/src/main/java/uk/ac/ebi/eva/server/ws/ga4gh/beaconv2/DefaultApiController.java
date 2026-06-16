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

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Generated;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.ac.ebi.eva.commons.beacon.models.Beacon;

@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-06-18T18:08:34.969Z[GMT]")
@Controller
@RequestMapping(value = "/v2/beacon")
public class DefaultApiController implements DefaultApi {

    private static final Logger log = LoggerFactory.getLogger(DefaultApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @Autowired
    private BeaconServiceV2 beaconServiceV2;

    @org.springframework.beans.factory.annotation.Autowired
    public DefaultApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<Beacon> getBeacon() {
        Beacon beacon = beaconServiceV2.getBeacon();
        return new ResponseEntity<Beacon>(beacon, HttpStatus.OK);
    }

}
