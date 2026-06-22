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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.eva.commons.beacon.models.BeaconAlleleRequest;
import uk.ac.ebi.eva.commons.beacon.models.BeaconAlleleResponse;
import uk.ac.ebi.eva.commons.beacon.models.Chromosome;

import java.util.Arrays;
import java.util.List;

@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-06-18T18:08:34.969Z[GMT]")
@Controller
@RequestMapping(value = "/v2/beacon")
public class QueryApiController implements QueryApi {

    private static final Logger log = LoggerFactory.getLogger(QueryApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @Autowired
    private BeaconServiceV2 beaconServiceV2;

    @org.springframework.beans.factory.annotation.Autowired
    public QueryApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<List<BeaconAlleleResponse>> getBeaconAlleleResponse(@NotNull @Parameter(description = "Reference name (chromosome). Accepting values 1-22, X, Y, MT.", required = true) @Valid @RequestParam(value = "referenceName", required = true) Chromosome referenceName,
                                                                              @NotNull @Pattern(regexp = "^([ACGT]+|N)$") @Parameter(description = "Reference bases for this variant (starting from `start`). Accepted values: [ACGT]* When querying for variants without specific base alterations (e.g. imprecise structural variants with separate variant_type as well as start_min & end_min ... parameters), the use of a single \"N\" value is required. ", required = true) @Valid @RequestParam(value = "referenceBases", required = true) String referenceBases,
                                                                              @NotNull @Parameter(description = "Assembly identifier (GRC notation, e.g. GRCh37).", required = true) @Valid @RequestParam(value = "assemblyId", required = true) String assemblyId,
                                                                              @Min(0L) @Parameter(description = "Precise start coordinate position, allele locus (0-based, inclusive). * start only:   - for single positions, e.g. the start of a specified sequence alteration where the size is given through the specified alternateBases   - typical use are queries for SNV and small InDels   - the use of \"start\" without an \"end\" parameter requires the use of \"referenceBases\" * start and end:   - special use case for exactly determined structural changes ") @Valid @RequestParam(value = "start", required = false) Long start,
                                                                              @Min(0L) @Parameter(description = "Minimum start coordinate * startMin + startMax + endMin + endMax   - for querying imprecise positions (e.g. identifying all structural variants starting anywhere between startMin <-> startMax, and ending anywhere between endMin <-> endMax)   - single or double sided precise matches can be achieved by setting startMin = startMax XOR endMin = endMax ") @Valid @RequestParam(value = "startMin", required = false) Long startMin,
                                                                              @Min(0L) @Parameter(description = "Maximum start coordinate. See startMin. ") @Valid @RequestParam(value = "startMax", required = false) Long startMax,
                                                                              @Min(0L) @Parameter(description = "Precise end coordinate (0-based, exclusive). See start. ") @Valid @RequestParam(value = "end", required = false) Long end,
                                                                              @Min(0L) @Parameter(description = "Minimum end coordinate. See startMin. ") @Valid @RequestParam(value = "endMin", required = false) Long endMin,
                                                                              @Min(0L) @Parameter(description = "Maximum end coordinate. See startMin. ") @Valid @RequestParam(value = "endMax", required = false) Long endMax,
                                                                              @Pattern(regexp = "^([ACGT]+|N)$") @Parameter(description = "The bases that appear instead of the reference bases. Accepted values: [ACGT]* or N. Symbolic ALT alleles (DEL, INS, DUP, INV, CNV, DUP:TANDEM, DEL:ME, INS:ME) will be represented in `variantType`. Optional: either `alternateBases` or `variantType` is required. ") @Valid @RequestParam(value = "alternateBases", required = false) String alternateBases,
                                                                              @Parameter(description = "The `variantType` is used to denote e.g. structural variants. Examples: * DUP: duplication of sequence following `start`; not necessarily in situ * DEL: deletion of sequence following `start` Optional: either `alternateBases` or `variantType` is required. ") @Valid @RequestParam(value = "variantType", required = false) String variantType,
                                                                              @Parameter(description = "Identifiers of datasets, as defined in \"BeaconDataset\". If this field is null/not specified, all datasets should be queried.") @Valid @RequestParam(value = "datasetIds", required = false) List<String> datasetIds,
                                                                              @Parameter(description = "Indicator of whether responses for individual datasets (datasetAlleleResponses) should be included in the response (BeaconAlleleResponse) to this request or not. If null (not specified), the default value of NONE is assumed. ", schema = @Schema(allowableValues = {"ALL, HIT, MISS, NONE"})) @Valid @RequestParam(value = "includeDatasetResponses", required = false) String includeDatasetResponses) {
        BeaconAlleleResponse response = beaconServiceV2.find(referenceName == null ? null : referenceName.toString(),
                start, startMin, startMax, end, endMin, endMax, referenceBases, alternateBases,
                variantType, assemblyId, datasetIds, includeDatasetResponses);
        if (response.getError() == null) {
            return new ResponseEntity<>(Arrays.asList(response), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Arrays.asList(response), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<List<BeaconAlleleResponse>> postBeaconAlleleResponse(@Parameter(description = "", required = true) @Valid @RequestBody BeaconAlleleRequest body) {
        BeaconAlleleResponse response = beaconServiceV2.find(body);
        if (response.getError() == null) {
            return new ResponseEntity<>(Arrays.asList(response), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Arrays.asList(response), HttpStatus.BAD_REQUEST);
        }
    }

}
