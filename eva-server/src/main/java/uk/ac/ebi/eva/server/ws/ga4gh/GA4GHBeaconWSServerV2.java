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

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.eva.commons.beacon.models.BeaconAlleleRequestBody;
import uk.ac.ebi.eva.commons.beacon.models.BeaconDataset;
import uk.ac.ebi.eva.commons.beacon.models.BeaconError;
import uk.ac.ebi.eva.commons.beacon.models.DatasetAlleleResponse;
import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.VariantType;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantMongo;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantSourceMongo;
import uk.ac.ebi.eva.commons.mongodb.filter.FilterBuilder;
import uk.ac.ebi.eva.commons.mongodb.filter.VariantRepositoryFilter;
import uk.ac.ebi.eva.commons.mongodb.services.AnnotationMetadataNotFoundException;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.server.ws.EvaWSServer;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;

@RestController
@RequestMapping(value = "/v2/beacon", produces = "application/json")
@Api(tags = {"ga4gh"})
public class GA4GHBeaconWSServerV2 extends EvaWSServer {

    @Autowired
    private VariantWithSamplesAndAnnotationsService service;


    public GA4GHBeaconWSServerV2() { }

    @GetMapping(value = "/")
    public GA4GHBeaconResponseV2 rootGet() {
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch37"));
        GA4GHBeaconResponseV2 response = new GA4GHBeaconResponseV2();
        List<BeaconDataset> beaconDatasets = new ArrayList<>();
        List<VariantSourceMongo> variantSourceMongos = service.findAllVariantSourcesForBeacon();
        variantSourceMongos.forEach(variantSourceMongo -> {
            beaconDatasets.add(new BeaconDataset(
                    variantSourceMongo.getStudyId(),
                    variantSourceMongo.getStudyName(),
                    "randomdescription",
                    "GRCh37",
                    variantSourceMongo.getDate() == null ? null : variantSourceMongo.getDate().toString(),
                    "updatedate",
                    variantSourceMongo.getStats() == null ? null : variantSourceMongo.getStats().getSamplesCount(),
                    variantSourceMongo.getStats() == null ? null : variantSourceMongo.getStats().getSamplesCount(),
                    null,
                    "externalurl",
                    "datausageconditions"
            ));
        });

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch38"));
        variantSourceMongos = service.findAllVariantSourcesForBeacon();
        variantSourceMongos.forEach(variantSourceMongo -> {
            beaconDatasets.add(new BeaconDataset(
                    variantSourceMongo.getStudyId(),
                    variantSourceMongo.getStudyName(),
                    "randomdescription",
                    "GRCh37",
                    variantSourceMongo.getDate() == null ? null : variantSourceMongo.getDate().toString(),
                    "updatedate",
                    variantSourceMongo.getStats() == null ? null : variantSourceMongo.getStats().getSamplesCount(),
                    variantSourceMongo.getStats() == null ? null : variantSourceMongo.getStats().getSamplesCount(),
                    null,
                    "externalurl",
                    "datausageconditions"
            ));
        });

        response.setDatasets(beaconDatasets);
        return response;
    }

    @GetMapping(value = "/query")
    public GA4GHBeaconQueryResponseV2 queryGet(@RequestParam("referenceName") String chromosome,
                                               @RequestParam(value = "start", required = false) Long start,
                                               @RequestParam(value = "startMin", required = false) Long startMin,
                                               @RequestParam(value = "startMax", required = false) Long startMax,
                                               @RequestParam(value = "end", required = false) Long end,
                                               @RequestParam(value = "endMin", required = false) Long endMin,
                                               @RequestParam(value = "endMax", required = false) Long endMax,
                                               @RequestParam(value = "referenceBases") String referenceBases,
                                               @RequestParam(value = "alternateBases", required = false)
                                                       String alternateBases,
                                               @RequestParam(value = "variantType", required = false) String variantType,
                                               @RequestParam(value = "assemblyId") String assemblyId,
                                               @RequestParam(value = "datasetIds", required = false) List<String> studies,
                                               @RequestParam(value = "includeDatasetResponses", required = false)
                                                       String includeDatasetResponses,
                                               HttpServletResponse response)
            throws IOException, AnnotationMetadataNotFoundException {

        initializeQuery();

        BeaconAlleleRequestBody request = new BeaconAlleleRequestBody(chromosome, start, startMin, startMax, end,
                endMin, endMax, referenceBases, alternateBases, variantType, assemblyId, studies,
                includeDatasetResponses);

        if (assemblyId.equalsIgnoreCase("grch37")) {
            MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch37"));
        } else if (assemblyId.equalsIgnoreCase("grch38")) {
            MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch38"));

        } else {
            return new GA4GHBeaconQueryResponseV2(GA4GHBeaconResponseV2.ID, GA4GHBeaconResponseV2.APIVERSION,
                    null, request, new BeaconError(HttpServletResponse.SC_BAD_REQUEST,
                    "Please enter a valid assemblyId from grch37,grch38"), null);
        }

        String errorMessage = checkErrorHelper(request);

        if (errorMessage != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new GA4GHBeaconQueryResponseV2(GA4GHBeaconResponseV2.ID, GA4GHBeaconResponseV2.APIVERSION,
                    null, request, new BeaconError(HttpServletResponse.SC_BAD_REQUEST, errorMessage), null);
        }


        VariantType type = variantType != null ? VariantType.valueOf(variantType) : null;

        Region startRange, endRange;

        startRange = start != null ? new Region(chromosome, start, start) : new Region(chromosome, startMin, startMax);

        endRange = end != null ? new Region(chromosome, end, end) : new Region(chromosome, endMin, endMax);

        List<VariantRepositoryFilter> filters = new FilterBuilder().getBeaconFilters(referenceBases, alternateBases,
                type, studies);

        Integer page_size = service.countByRegionAndOtherBeaconFilters(startRange, endRange, filters).intValue();
        Pageable pageable;
        List<VariantMongo> variantMongoList;

        if (page_size > 0) {
            pageable = new PageRequest(0, page_size);
            variantMongoList = service.findByRegionAndOtherBeaconFilters(startRange, endRange, filters, pageable);

        } else {
            pageable = null;
            variantMongoList = Collections.emptyList();
        }

        List<DatasetAlleleResponse> datasetAlleleResponses = getDatasetAlleleResponsesHelper(variantMongoList, request);

        if (variantMongoList.size() > 0) {
            return new GA4GHBeaconQueryResponseV2(GA4GHBeaconResponseV2.ID, GA4GHBeaconResponseV2.APIVERSION,
                    true, request, null, datasetAlleleResponses);
        } else {
            return new GA4GHBeaconQueryResponseV2(GA4GHBeaconResponseV2.ID, GA4GHBeaconResponseV2.APIVERSION,
                    false, request, null, datasetAlleleResponses);
        }
    }

    private String checkErrorHelper(BeaconAlleleRequestBody request) {

        if (request.getStart() != null && request.getStart() < 0) {
            return "please provide a positive start number";
        }

        if (request.getEnd() != null && request.getEnd() < 0) {
            return "pleaseprovide a positive end number";
        }

        if (request.getAlternateBases() == null && request.getVariantType() == null) {
            return "Either alternateBases or variantType is required";
        }

        if (request.getVariantType() != null) {
            try {
                VariantType variantType = VariantType.valueOf(request.getVariantType());
            } catch (Exception e) {
                String errorMessage = "Please provide a valid variant type from ";
                return errorMessage + String.join(", ", Arrays.asList(VariantType.values()).toString());
            }
        }

        return null;
    }

    private List<DatasetAlleleResponse> getDatasetAlleleResponsesHelper(List<VariantMongo> variantMongoList,
                                                                        BeaconAlleleRequestBody request) {

        List<DatasetAlleleResponse> datasetAllelResponses = new ArrayList<DatasetAlleleResponse>();

        if (request.getIncludeDatasetResponses() == null ||
                request.getIncludeDatasetResponses().equalsIgnoreCase("NONE")) {
            return null;
        }

        List<VariantSourceMongo> variantSourceMongoList = service.findAllVariantSourcesForBeacon();

        HashSet<String> studiesPresent = new HashSet<String>();
        HashMap<String, Float> studyIdToFrequencyMapper = new HashMap<>();
        variantMongoList.forEach(variantMongo -> {
            variantMongo.getSourceEntries().forEach(variantSourceEntryMongo -> {
                studiesPresent.add(variantSourceEntryMongo.getStudyId());
            });
            variantMongo.getVariantStatsMongo().forEach(variantStatisticsMongo -> {
                if (variantMongo.getAlternate().equalsIgnoreCase(variantStatisticsMongo.getMafAllele())) {
                    studyIdToFrequencyMapper.put(variantStatisticsMongo.getStudyId(), variantStatisticsMongo.getMaf());
                }
            });
        });

        HashMap<String, VariantSourceMongo> allStudies = new HashMap<>();
        variantSourceMongoList.forEach(variantSourceMongo -> {
            allStudies.put(variantSourceMongo.getStudyId(), variantSourceMongo);
        });

        allStudies.forEach((studyId, variantSourceMongo) -> {
            if (studiesPresent.contains(studyId)) {
                if (request.getIncludeDatasetResponses().equalsIgnoreCase("ALL") ||
                        request.getIncludeDatasetResponses().equalsIgnoreCase("HIT")) {
                    datasetAllelResponses.add(buildDatasetAlleleResponseHelper(true,
                            variantSourceMongo,
                            studyIdToFrequencyMapper.get(variantSourceMongo.getStudyId()) == null ?
                                    null : new Float(studyIdToFrequencyMapper.get(variantSourceMongo.getStudyId()))));
                }
            } else {
                if (request.getIncludeDatasetResponses().equalsIgnoreCase("ALL") ||
                        request.getIncludeDatasetResponses().equalsIgnoreCase("MISS")) {
                    datasetAllelResponses.add(buildDatasetAlleleResponseHelper(false,
                            variantSourceMongo, null));
                }
            }
        });

        return datasetAllelResponses;
    }

    private DatasetAlleleResponse buildDatasetAlleleResponseHelper(boolean exists, VariantSourceMongo variantSourceMongo,
                                                                   Float frequency) {
        return new DatasetAlleleResponse(variantSourceMongo.getStudyId(), exists, null, frequency,
                variantSourceMongo.getStats() == null ? null : (long) variantSourceMongo.getStats().getVariantsCount(),
                null,
                variantSourceMongo.getStats() == null ? null : (long) variantSourceMongo.getStats().getSamplesCount(),
                "noteString", "externalUrl", null);
    }

    @PostMapping(value = "/query")
    private GA4GHBeaconQueryResponseV2 queryPost(@Validated @RequestBody BeaconAlleleRequestBody requestBody,
                                                 HttpServletResponse response) throws IOException,
            AnnotationMetadataNotFoundException {

        return queryGet(requestBody.getReferenceName(),
                requestBody.getStart(),
                requestBody.getStartMin(),
                requestBody.getStartMax(),
                requestBody.getEnd(),
                requestBody.getEndMin(),
                requestBody.getEndMax(),
                requestBody.getReferenceBases(),
                requestBody.getAlternateBases(),
                requestBody.getVariantType(),
                requestBody.getAssemblyId(),
                requestBody.getDatasetIds(),
                requestBody.getIncludeDatasetResponses(),
                response);
    }
}
