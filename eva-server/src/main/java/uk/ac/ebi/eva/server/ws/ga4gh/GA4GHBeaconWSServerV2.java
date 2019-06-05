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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.VariantType;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantMongo;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantSourceMongo;
import uk.ac.ebi.eva.commons.mongodb.filter.FilterBuilder;
import uk.ac.ebi.eva.commons.mongodb.filter.VariantRepositoryFilter;
import uk.ac.ebi.eva.commons.mongodb.services.AnnotationMetadataNotFoundException;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.server.ws.EvaWSServer;


@RestController
@RequestMapping(value = "/v2/beacon", produces = "application/json")
@Api(tags = {"ga4gh"})
public class GA4GHBeaconWSServerV2 extends EvaWSServer {

    protected static Logger logger = LoggerFactory.getLogger(GA4GHBeaconWSServer.class);
    @Autowired
    private VariantWithSamplesAndAnnotationsService service;


    public GA4GHBeaconWSServerV2() {
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public GA4GHBeaconResponseV2 rootGet() {
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch37"));
        GA4GHBeaconResponseV2 response = new GA4GHBeaconResponseV2();
        List<BeaconDataset> beaconDatasets = new ArrayList<>();

        List<VariantSourceMongo> variantSourceMongos = service.findAllForBeacon();
        variantSourceMongos.forEach(variantSourceMongo -> {
            beaconDatasets.add(new BeaconDataset(
                    variantSourceMongo.getStudyId(),
                    variantSourceMongo.getStudyName(),
                    "randomdescription",
                    "GRCh37",
                    variantSourceMongo.getDate().toString(),
                    "updatedate"
            ));
        });

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch38"));
        variantSourceMongos = service.findAllForBeacon();
        variantSourceMongos.forEach(variantSourceMongo -> {
            beaconDatasets.add(new BeaconDataset(
                    variantSourceMongo.getStudyId(),
                    variantSourceMongo.getStudyName(),
                    "randomdescription",
                    "GRCh38",
                    variantSourceMongo.getDate().toString(),
                    "updatedate"
            ));
        });


        response.setBeaconDatasetList(beaconDatasets);
        return response;
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET)
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
            return new GA4GHBeaconQueryResponseV2(GA4GHBeaconResponseV2.id_val, GA4GHBeaconResponseV2.apiVersion_val,
                    null, request, new BeaconError(HttpServletResponse.SC_BAD_REQUEST,
                    "Please enter a valid assemblyId"), null);
        }

        String errorMessage = checkErrorHelper(request);

        if (errorMessage != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new GA4GHBeaconQueryResponseV2(GA4GHBeaconResponseV2.id_val,
                    GA4GHBeaconResponseV2.apiVersion_val, null, request,
                    new BeaconError(HttpServletResponse.SC_BAD_REQUEST, errorMessage), null);
        }

        VariantType variantType1;
        try {
            variantType1 = VariantType.valueOf(variantType);
        } catch (Exception e) {
            variantType1 = null;
        }

        Region startRange, endRange;

        if (start != null) {
            startRange = new Region(chromosome, start, start);
        } else {
            startRange = new Region(chromosome, startMin, startMax);
        }

        if (end != null) {
            endRange = new Region(chromosome, end, end);
        } else {
            endRange = new Region(chromosome, endMin, endMax);
        }

        List<VariantRepositoryFilter> filters = new FilterBuilder().getBeaconFilters(referenceBases, alternateBases,
                variantType1, studies);

        List<VariantMongo> variantMongoList = service.findbyRegionAndOtherBeaconFilters(startRange, endRange, filters);

        List<DatasetAlleleResponse> datasetAlleleResponses = getDatasetAlleleResponsesHelper(variantMongoList, request);

        if (variantMongoList.size() > 0) {
            return new GA4GHBeaconQueryResponseV2(GA4GHBeaconResponseV2.id_val,
                    GA4GHBeaconResponseV2.apiVersion_val, true, request, null,
                    datasetAlleleResponses);
        } else {
            return new GA4GHBeaconQueryResponseV2(GA4GHBeaconResponseV2.id_val,
                    GA4GHBeaconResponseV2.apiVersion_val, false, request, null,
                    datasetAlleleResponses);
        }
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public GA4GHBeaconQueryResponseV2 queryPost(@Validated @RequestBody BeaconAlleleRequestBody requestBody,
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

    public String checkErrorHelper(BeaconAlleleRequestBody request) {

        if (request.getStart() != null && request.getStart() < 0) {
            return "please provide a positive start number";
        }

        if (request.getEnd() != null && request.getEnd() < 0) {
            return "pleaseprovide a positive end number";
        }

        if (request.getAlternateBases() == null && request.getVariantType() == null) {
            return "Either alternateBases ot variantType is required";
        }

        return null;
    }

    public List<DatasetAlleleResponse> getDatasetAlleleResponsesHelper(List<VariantMongo> variantMongoList,
                                                                       BeaconAlleleRequestBody request) {

        List<DatasetAlleleResponse> datasetAllelResponses = new ArrayList<DatasetAlleleResponse>();

        if (request.getIncludeDatasetResponses() == null ||
                request.getIncludeDatasetResponses().equalsIgnoreCase("NONE")) {
            return null;
        }

        Set<String> allDistinctDatasetIds = service.findAllDistinctDatasetIds();

        HashSet<String> studiesPresent = new HashSet<String>();
        variantMongoList.forEach(variantMongo -> variantMongo.getSourceEntries()
                .forEach(variantSourceEntryMongo -> studiesPresent.add(variantSourceEntryMongo.getStudyId())));

        if (request.getIncludeDatasetResponses().equalsIgnoreCase("HIT")) {
            studiesPresent.forEach(study -> {
                datasetAllelResponses.add(new DatasetAlleleResponse(study, true));
            });

        } else if (request.getIncludeDatasetResponses().equalsIgnoreCase("Miss")) {
            if (allDistinctDatasetIds != null) {
                allDistinctDatasetIds.forEach(study -> {
                    if (!studiesPresent.contains(study)) {
                        datasetAllelResponses.add(new DatasetAlleleResponse(study, false));
                    }
                });
            }
        } else if (request.getIncludeDatasetResponses().equalsIgnoreCase("ALL")) {
            if (allDistinctDatasetIds != null) {
                allDistinctDatasetIds.forEach(study -> {
                    if (!studiesPresent.contains(study)) {
                        datasetAllelResponses.add(new DatasetAlleleResponse(study, false));
                    } else {
                        datasetAllelResponses.add(new DatasetAlleleResponse(study, true));
                    }
                });
            }
        }

        return datasetAllelResponses;
    }
}
