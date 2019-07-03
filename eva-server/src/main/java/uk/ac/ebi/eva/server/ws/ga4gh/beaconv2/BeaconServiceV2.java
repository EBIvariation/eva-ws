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
import org.springframework.stereotype.Service;
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

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;

import static uk.ac.ebi.eva.commons.beacon.models.BeaconAlleleRequest.IncludeDatasetResponsesEnum;

@Service
public class BeaconServiceV2 {

    @Autowired
    private VariantWithSamplesAndAnnotationsService service;

    @Autowired
    private VariantSourceService variantSourceService;

    public BeaconServiceV2() {
    }

    public BeaconImpl getBeacon() {
        BeaconImpl beacon = new BeaconImpl();
        beacon.setDatasets(getAllBeaconDatasets());
        return beacon;
    }

    private List<BeaconDataset> getAllBeaconDatasets() {
        List<BeaconDataset> beaconDatasets = new ArrayList<>();
        beaconDatasets.addAll(getBeaconDatasetsPerDatabase("hsapiens", "GRCh37"));
        beaconDatasets.addAll(getBeaconDatasetsPerDatabase("hsapiens", "GRCh38"));
        return beaconDatasets;
    }

    private List<BeaconDataset> getBeaconDatasetsPerDatabase(String species, String assemblyId) {
        List<BeaconDataset> beaconDatasets = new ArrayList<>();
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species + "_" +
                assemblyId.toLowerCase()));
        List<VariantSource> variantSources = variantSourceService.findAllVariantSourcesForBeacon();
        variantSources.forEach(
                variantSource -> beaconDatasets.add(
                        new BeaconDataset().id(variantSource.getStudyId())
                                .name(variantSource.getStudyName())
                                .assemblyId(assemblyId)
                                .updateDateTime(variantSource.getDate() == null ? null :
                                        variantSource.getDate().toString())
                                .sampleCount(variantSource.getStats() == null ? null :
                                        (long) variantSource.getStats().getSamplesCount())
                                .variantCount(variantSource.getStats() == null ? null :
                                        (long) variantSource.getStats().getVariantsCount())));
        return beaconDatasets;
    }

    public BeaconAlleleResponse find(String chromosome, Long start, Long startMin, Long startMax, Long end, Long endMin,
                                     Long endMax, String referenceBases, String alternateBases, String variantType,
                                     String assemblyId, List<String> studies, String includeDatasetResponses) {

        try {
            checkParameters(chromosome, referenceBases, start, end, alternateBases, variantType, assemblyId,
                    includeDatasetResponses);
        } catch (IllegalArgumentException e) {
            return buildBeaconAlleleResponse(null, null, null, e.getMessage());
        }

        BeaconAlleleRequest request = buildBeaconAlleleRequest(chromosome, start, startMin, startMax, end, endMin,
                endMax, referenceBases, alternateBases, variantType, assemblyId, studies, includeDatasetResponses);

        VariantType type = variantType != null ? VariantType.valueOf(variantType) : null;

        Region startRange = start != null ? new Region(chromosome, start, start) : new Region(chromosome, startMin,
                startMax);
        Region endRange = end != null ? new Region(chromosome, end, end) : new Region(chromosome, endMin, endMax);

        List<VariantRepositoryFilter> filters = new FilterBuilder().getBeaconFilters(referenceBases, alternateBases,
                type, studies);

        Integer pageSize = service.countByRegionAndOtherBeaconFilters(startRange, endRange, filters).intValue();
        List<VariantMongo> variantMongoList;

        if (pageSize > 0) {
            variantMongoList = service.findByRegionAndOtherBeaconFilters(startRange, endRange, filters,
                    new PageRequest(0, pageSize));
        } else {
            variantMongoList = Collections.emptyList();
        }

        if (variantMongoList.size() > 0) {
            return buildBeaconAlleleResponse(true, request, buildDatasetAlleleResponses(variantMongoList,
                    request), null);
        } else {
            return buildBeaconAlleleResponse(false, request, buildDatasetAlleleResponses(variantMongoList,
                    request), null);
        }

    }

    private void checkParameters(String chromosome, String referenceBases, Long start, Long end,
                                 String alternateBases, String variantType, String assemblyId,
                                 String includeDatasetResponses) throws IllegalArgumentException {
        if (chromosome == null || chromosome.length() == 0) {
            String errorMessage = "Please provide a valid reference name from ";
            throw new IllegalArgumentException(errorMessage + String.join(", ",
                    Arrays.asList(Chromosome.values()).toString()));
        } else {
            try {
                Chromosome.fromValue(chromosome);
            } catch (Exception e) {
                String errorMessage = "Please provide a valid reference name from ";
                throw new IllegalArgumentException(errorMessage + String.join(", ", Arrays.asList(Chromosome.values())
                        .toString()));
            }
        }
        if (assemblyId == null || assemblyId.length() == 0) {
            throw new IllegalArgumentException("Please provide an assembly identifier");
        }

        if (referenceBases == null || referenceBases.length() == 0) {
            throw new IllegalArgumentException("Please provide reference bases");
        }

        if (start != null && start < 0) {
            throw new IllegalArgumentException("Please provide a positive start number");
        }

        if (end != null && end < 0) {
            throw new IllegalArgumentException("Please provide a positive end number");
        }

        if (alternateBases == null && variantType == null) {
            throw new IllegalArgumentException("Either the alternate bases or the variant type is required");
        }

        if (variantType != null) {
            try {
                VariantType.valueOf(variantType);
            } catch (Exception e) {
                String errorMessage = "Please provide a valid variant type from ";
                throw new IllegalArgumentException(errorMessage + String.join(", ", Arrays.asList(VariantType.values())
                        .toString()));
            }
        }

        if (includeDatasetResponses != null) {
            try {
                BeaconAlleleRequest.IncludeDatasetResponsesEnum.valueOf(includeDatasetResponses);
            } catch (Exception e) {
                String errorMessage = "Please provide a valid dataset inclusion flag from ";
                throw new IllegalArgumentException(errorMessage + String.join(", ", Arrays.asList(BeaconAlleleRequest.
                        IncludeDatasetResponsesEnum.values()).toString()));
            }
        }

        if (assemblyId.equalsIgnoreCase("grch37")) {
            MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch37"));
        } else if (assemblyId.equalsIgnoreCase("grch38")) {
            MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch38"));
        } else {
            String errorMessage = "Please enter a valid assembly name (GRCh37 or GRCh38)";
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private BeaconAlleleResponse buildBeaconAlleleResponse(Boolean exists, BeaconAlleleRequest request,
                                                           List<BeaconDatasetAlleleResponse> datasetAlleleResponses,
                                                           String errorMessage) {
        if (errorMessage == null) {
            return new BeaconAlleleResponse()
                    .beaconId(BeaconImpl.ID)
                    .apiVersion(BeaconImpl.API_VERSION)
                    .exists(exists)
                    .alleleRequest(request)
                    .datasetAlleleResponses(datasetAlleleResponses);
        } else {
            return new BeaconAlleleResponse()
                    .beaconId(BeaconImpl.ID)
                    .apiVersion(BeaconImpl.API_VERSION)
                    .error(new BeaconError().errorCode(HttpServletResponse.SC_BAD_REQUEST)
                            .errorMessage(errorMessage));
        }
    }

    private BeaconAlleleRequest buildBeaconAlleleRequest(String chromosome, Long start, Long startMin, Long startMax,
                                                         Long end, Long endMin, Long endMax, String referenceBases,
                                                         String alternateBases, String variantType, String assemblyId,
                                                         List<String> studies, String includeDatasetResponses) {
        return new BeaconAlleleRequest()
                .referenceName(Chromosome.fromValue(chromosome))
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
                .datasetIds(studies)
                .includeDatasetResponses(includeDatasetResponses == null ? null : IncludeDatasetResponsesEnum
                        .valueOf(includeDatasetResponses));
    }

    private List<BeaconDatasetAlleleResponse> buildDatasetAlleleResponses(List<VariantMongo> variantMongoList,
                                                                          BeaconAlleleRequest request) {
        List<BeaconDatasetAlleleResponse> datasetAllelResponses = new ArrayList<BeaconDatasetAlleleResponse>();

        if (request.getIncludeDatasetResponses() == null ||
                request.getIncludeDatasetResponses().equals(BeaconAlleleRequest.IncludeDatasetResponsesEnum.NONE)) {
            return null;
        }

        List<VariantSource> variantSourceList = variantSourceService.findAllVariantSourcesForBeacon();

        HashSet<String> datasetIdsPresent = new HashSet<>();
        HashMap<String, Float> datasetIdToFrequencyMapper = new HashMap<>();

        variantMongoList.forEach(variantMongo -> {
            variantMongo.getSourceEntries().forEach(variantSourceEntryMongo -> {
                datasetIdsPresent.add(variantSourceEntryMongo.getStudyId() + "_" + variantSourceEntryMongo.getFileId());
            });
            variantMongo.getVariantStatsMongo().forEach(variantStatisticsMongo -> {
                if (variantMongo.getAlternate().equalsIgnoreCase(variantStatisticsMongo.getMafAllele()) ||
                        variantMongo.getReference().equalsIgnoreCase(variantStatisticsMongo.getMafAllele())) {
                    datasetIdToFrequencyMapper.put(variantStatisticsMongo.getStudyId() + "_" + variantStatisticsMongo
                                    .getFileId(), variantStatisticsMongo.getMaf());
                }
            });
        });

        HashMap<String, VariantSource> allDatasetIds = new HashMap<>();
        variantSourceList.forEach(variantSource -> {
            allDatasetIds.put(variantSource.getStudyId() + "_" + variantSource.getFileId(), variantSource);
        });

        allDatasetIds.forEach((datasetId, variantSource) -> {
            if (datasetIdsPresent.contains(datasetId)) {
                if (request.getIncludeDatasetResponses().equals(BeaconAlleleRequest.IncludeDatasetResponsesEnum.ALL) ||
                        request.getIncludeDatasetResponses().equals(BeaconAlleleRequest
                                .IncludeDatasetResponsesEnum.HIT)) {
                    datasetAllelResponses.add(buildDatasetAlleleResponseHelper(true,
                            variantSource,
                            datasetIdToFrequencyMapper.get(variantSource.getStudyId()) == null ?
                                    null : new Float(datasetIdToFrequencyMapper.get(variantSource.getStudyId()))));
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
                .datasetId(variantSource.getStudyId() + "_" + variantSource.getFileId())
                .exists(exists)
                .frequency(frequency == null ? null : new BigDecimal(frequency))
                .variantCount(variantSource.getStats() == null ? null : (long) variantSource.getStats().
                        getVariantsCount())
                .sampleCount(variantSource.getStats() == null ? null : (long) variantSource.getStats().
                        getSamplesCount())
                .note("notestring")
                .externalUrl("enternalUrl");
    }

    public BeaconAlleleResponse find(BeaconAlleleRequest requestBody) {
        return find(requestBody.getReferenceName().toString(),
                requestBody.getStart(),
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
