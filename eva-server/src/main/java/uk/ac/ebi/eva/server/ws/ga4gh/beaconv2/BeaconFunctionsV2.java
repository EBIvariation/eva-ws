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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import uk.ac.ebi.eva.commons.beacon.models.BeaconAlleleRequest;
import uk.ac.ebi.eva.commons.beacon.models.BeaconError;
import uk.ac.ebi.eva.commons.beacon.models.BeaconAlleleResponse;
import uk.ac.ebi.eva.commons.beacon.models.BeaconDatasetAlleleResponse;
import uk.ac.ebi.eva.commons.beacon.models.BeaconDataset;
import uk.ac.ebi.eva.commons.beacon.models.Chromosome;
import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.VariantSource;
import uk.ac.ebi.eva.commons.core.models.VariantType;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantMongo;
import uk.ac.ebi.eva.commons.mongodb.filter.FilterBuilder;
import uk.ac.ebi.eva.commons.mongodb.filter.VariantRepositoryFilter;
import uk.ac.ebi.eva.commons.mongodb.services.VariantSourceService;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.server.ws.EvaWSServer;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;

@Component
public class BeaconFunctionsV2 extends EvaWSServer {

    @Autowired
    private VariantWithSamplesAndAnnotationsService service;

    @Autowired
    private VariantSourceService variantSourceService;

    public BeaconFunctionsV2() {
    }

    public BeaconImpl rootGet() {
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch37"));
        BeaconImpl response = new BeaconImpl();
        List<BeaconDataset> beaconDatasets = new ArrayList<>();
        List<VariantSource> variantSourceMongos = variantSourceService.findAllVariantSourcesForBeacon();
        variantSourceMongos.forEach(variantSourceMongo -> {
            beaconDatasets.add(new BeaconDataset()
                    .id(variantSourceMongo.getStudyId())
                    .name(variantSourceMongo.getStudyName())
                    .description("randomDescription")
                    .assemblyId("GRCh37")
                    .createDateTime(variantSourceMongo.getDate() == null ? null : variantSourceMongo.getDate()
                            .toString())
                    .updateDateTime("updatedatetime")
                    .sampleCount(variantSourceMongo.getStats() == null ? null : (long) variantSourceMongo.getStats()
                            .getSamplesCount())
                    .variantCount(variantSourceMongo.getStats() == null ? null :
                            (long) variantSourceMongo.getStats().getVariantsCount())
                    .externalUrl("externalurl"));
        });

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch38"));
        variantSourceMongos = variantSourceService.findAllVariantSourcesForBeacon();
        variantSourceMongos.forEach(variantSourceMongo -> {
            beaconDatasets.add(new BeaconDataset()
                    .id(variantSourceMongo.getStudyId())
                    .name(variantSourceMongo.getStudyName())
                    .description("randomDescription")
                    .assemblyId("GRCh38")
                    .createDateTime(variantSourceMongo.getDate() == null ? null : variantSourceMongo.getDate()
                            .toString())
                    .updateDateTime("updatedatetime")
                    .sampleCount(variantSourceMongo.getStats() == null ? null : (long) variantSourceMongo.getStats()
                            .getSamplesCount())
                    .variantCount(variantSourceMongo.getStats() == null ? null :
                            (long) variantSourceMongo.getStats().getVariantsCount())
                    .externalUrl("externalurl"));
        });

        response.setDatasets(beaconDatasets);
        return response;
    }

    public ResponseEntity<List<BeaconAlleleResponse>> queryGet(String chromosome, Long start, Long startMin,
                                                               Long startMax, Long end, Long endMin,
                                                               Long endMax, String referenceBases,
                                                               String alternateBases, String variantType,
                                                               String assemblyId, List<String> studies,
                                                               String includeDatasetResponses) {
        initializeQuery();

        String errorMessage = checkErrorHelper(chromosome, referenceBases, start, end, alternateBases, variantType,
                assemblyId, includeDatasetResponses);

        if (errorMessage != null) {
            return new ResponseEntity<List<BeaconAlleleResponse>>(Arrays.asList(new BeaconAlleleResponse()
                    .beaconId(BeaconImpl.ID)
                    .apiVersion(BeaconImpl.APIVERSION)
                    .error(new BeaconError().errorCode(HttpServletResponse.SC_BAD_REQUEST)
                            .errorMessage(errorMessage))), HttpStatus.BAD_REQUEST);
        }

        BeaconAlleleRequest request = new BeaconAlleleRequest()
                .referenceName(Chromosome.valueOf(chromosome))
                .start(start)
                .startMin(startMin == null ? null : startMin.intValue())
                .startMax(startMax == null ? null : startMax.intValue())
                .end(end == null ? null : end.intValue())
                .endMin(endMin == null ? null : endMin.intValue())
                .endMax(endMax == null ? null : endMax.intValue())
                .referenceBases(referenceBases)
                .alternateBases(alternateBases)
                .variantType(variantType)
                .assemblyId(assemblyId)
                .datasetIds(studies);
        if (includeDatasetResponses != null) {
            request = request.includeDatasetResponses(BeaconAlleleRequest.IncludeDatasetResponsesEnum
                    .valueOf(includeDatasetResponses));
        }

        if (assemblyId.equalsIgnoreCase("grch37")) {
            MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch37"));
        } else if (assemblyId.equalsIgnoreCase("grch38")) {
            MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch38"));

        } else {
            return new ResponseEntity<List<BeaconAlleleResponse>>(Arrays.asList(new BeaconAlleleResponse()
                    .beaconId(BeaconImpl.ID)
                    .apiVersion(BeaconImpl.APIVERSION)
                    .error(new BeaconError().errorCode(HttpServletResponse.SC_BAD_REQUEST).errorMessage
                            ("Please enter a valid assemblyId from grch37,grch38"))), HttpStatus.BAD_REQUEST);
        }

        VariantType type = variantType != null ? VariantType.valueOf(variantType) : null;

        Region startRange, endRange;
        startRange = start != null ? new Region(chromosome, start, start) : new Region(chromosome, startMin, startMax);

        endRange = end != null ? new Region(chromosome, end, end) : new Region(chromosome, endMin, endMax);

        List<VariantRepositoryFilter> filters = new FilterBuilder().getBeaconFilters(referenceBases, alternateBases,
                type, studies);

        Integer page_size = service.countByRegionAndOtherBeaconFilters(startRange, endRange, filters).intValue();
        List<VariantMongo> variantMongoList;
        if (page_size > 0) {
            variantMongoList = service.findByRegionAndOtherBeaconFilters(startRange, endRange, filters,
                    new PageRequest(0, page_size));
        } else {
            variantMongoList = Collections.emptyList();
        }

        List<BeaconDatasetAlleleResponse> datasetAlleleResponses = getDatasetAlleleResponsesHelper(variantMongoList,
                request);

        if (variantMongoList.size() > 0) {
            return new ResponseEntity<List<BeaconAlleleResponse>>(Arrays.asList(new BeaconAlleleResponse()
                    .beaconId(BeaconImpl.ID)
                    .apiVersion(BeaconImpl.APIVERSION)
                    .exists(true)
                    .alleleRequest(request)
                    .datasetAlleleResponses(datasetAlleleResponses)), HttpStatus.BAD_REQUEST);

        } else {
            return new ResponseEntity<List<BeaconAlleleResponse>>(Arrays.asList(new BeaconAlleleResponse()
                    .beaconId(BeaconImpl.ID)
                    .apiVersion(BeaconImpl.APIVERSION)
                    .exists(false)
                    .alleleRequest(request)
                    .datasetAlleleResponses(datasetAlleleResponses)), HttpStatus.BAD_REQUEST);
        }
    }

    private String checkErrorHelper(String chromosome, String referenceBases, Long start, Long end,
                                    String alternateBases, String variantType, String assemblyId,
                                    String includeDatasetResponses) {
        if (chromosome == null || chromosome.length() == 0) {
            return "A reference name must be provided";
        } else {
            try {
                Chromosome chromosome1 = Chromosome.valueOf(chromosome);
            } catch (Exception e) {
                String errorMessage = "Please provide a valid referenceName type from ";
                return errorMessage + String.join(", ", Arrays.asList(Chromosome.values()).toString());
            }
        }
        if (assemblyId == null || assemblyId.length() == 0) {
            return "An assemblyId must be provided";
        }

        if (referenceBases == null || referenceBases.length() == 0) {
            return "ReferenceBases must be provided";
        }

        if (start != null && start < 0) {
            return "Please provide a positive start number";
        }

        if (end != null && end < 0) {
            return "Please provide a positive end number";
        }

        if (alternateBases == null && variantType == null) {
            return "Either alternateBases or variantType is required";
        }

        if (variantType != null) {
            try {
                VariantType variantType1 = VariantType.valueOf(variantType);
            } catch (Exception e) {
                String errorMessage = "Please provide a valid variant type from ";
                return errorMessage + String.join(", ", Arrays.asList(VariantType.values()).toString());
            }
        }

        if (includeDatasetResponses != null) {
            try {
                BeaconAlleleRequest.IncludeDatasetResponsesEnum includeDatasetResponsesEnum =
                        BeaconAlleleRequest.IncludeDatasetResponsesEnum.valueOf(includeDatasetResponses);
            } catch (Exception e) {
                String errorMessage = "Please provide a valid includeDatasetResponses from ";
                return errorMessage + String.join(", ", Arrays.asList(BeaconAlleleRequest.
                        IncludeDatasetResponsesEnum.values()).toString());
            }
        }
        return null;
    }

    private List<BeaconDatasetAlleleResponse> getDatasetAlleleResponsesHelper(List<VariantMongo> variantMongoList,
                                                                              BeaconAlleleRequest request) {

        List<BeaconDatasetAlleleResponse> datasetAllelResponses = new ArrayList<BeaconDatasetAlleleResponse>();

        if (request.getIncludeDatasetResponses() == null ||
                request.getIncludeDatasetResponses().equals(BeaconAlleleRequest.IncludeDatasetResponsesEnum.NONE)) {
            return null;
        }

        List<VariantSource> variantSourceList = variantSourceService.findAllVariantSourcesForBeacon();

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

        HashMap<String, VariantSource> allStudies = new HashMap<>();
        variantSourceList.forEach(variantSource -> {
            allStudies.put(variantSource.getStudyId(), variantSource);
        });

        allStudies.forEach((studyId, variantSource) -> {
            if (studiesPresent.contains(studyId)) {
                if (request.getIncludeDatasetResponses().equals(BeaconAlleleRequest.IncludeDatasetResponsesEnum.ALL) ||
                        request.getIncludeDatasetResponses().equals(BeaconAlleleRequest
                                .IncludeDatasetResponsesEnum.HIT)) {
                    datasetAllelResponses.add(buildDatasetAlleleResponseHelper(true,
                            variantSource,
                            studyIdToFrequencyMapper.get(variantSource.getStudyId()) == null ?
                                    null : new Float(studyIdToFrequencyMapper.get(variantSource.getStudyId()))));
                }
            } else {
                if (request.getIncludeDatasetResponses().equals(BeaconAlleleRequest.IncludeDatasetResponsesEnum.ALL) ||
                        request.getIncludeDatasetResponses().equals(BeaconAlleleRequest
                                .IncludeDatasetResponsesEnum.MISS)) {
                    datasetAllelResponses.add(buildDatasetAlleleResponseHelper(false,
                            variantSource, null));
                }
            }
        });

        return datasetAllelResponses;
    }

    private BeaconDatasetAlleleResponse buildDatasetAlleleResponseHelper(boolean exists, VariantSource variantSource,
                                                                         Float frequency) {
        return new BeaconDatasetAlleleResponse()
                .datasetId(variantSource.getStudyId())
                .exists(exists)
                .frequency(frequency == null ? null : new BigDecimal(frequency))
                .variantCount(variantSource.getStats() == null ? null : (long) variantSource.getStats().
                        getVariantsCount())
                .sampleCount(variantSource.getStats() == null ? null : (long) variantSource.getStats().
                        getSamplesCount())
                .note("notestring")
                .externalUrl("enternalUrl");
    }

    @PostMapping(value = "/query")
    public ResponseEntity<List<BeaconAlleleResponse>> queryPost(BeaconAlleleRequest requestBody) {

        return queryGet(requestBody.getReferenceName().toString(),
                requestBody.getStart() == null ? null : requestBody.getStart(),
                requestBody.getStartMin() == null ? null : (long) requestBody.getStartMin(),
                requestBody.getStartMax() == null ? null : (long) requestBody.getStartMax(),
                requestBody.getEnd() == null ? null : (long) requestBody.getEnd(),
                requestBody.getEndMin() == null ? null : (long) requestBody.getEndMin(),
                requestBody.getEndMax() == null ? null : (long) requestBody.getEndMax(),
                requestBody.getReferenceBases(),
                requestBody.getAlternateBases(),
                requestBody.getVariantType(),
                requestBody.getAssemblyId(),
                requestBody.getDatasetIds(),
                requestBody.getIncludeDatasetResponses().toString());
    }

}