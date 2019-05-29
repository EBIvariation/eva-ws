package uk.ac.ebi.eva.server.ws.ga4gh;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.eva.commons.core.models.VariantType;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantMongo;
import uk.ac.ebi.eva.commons.mongodb.filter.FilterBuilder;
import uk.ac.ebi.eva.commons.mongodb.filter.VariantRepositoryFilter;
import uk.ac.ebi.eva.commons.mongodb.services.AnnotationMetadataNotFoundException;
import uk.ac.ebi.eva.commons.mongodb.services.VariantSourceService;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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
    @Autowired
    private VariantSourceService sourceService;

    public GA4GHBeaconWSServerV2() {
    }

   /* @PostMapping("/query")
    public GA4GHBeaconQueryResponseV2 postRequest(@Valid @RequestBody BeaconAlleleRequestBody request, HttpServletResponse response) throws AnnotationMetadataNotFoundException,IOException{
        return query(request.getReferenceName(),request.getStart(),request.getStartMin(),request.getEndMax(),request.getEnd(),request.getEndMin(),request.getEndMax(),request.getReferenceBases(),request.getAlternateBases(),request.getVariantType(),request.getAssemblyId(),request.getDatasetIds(),request.getIncludeDatasetResponses(),response);
    }*/

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public GA4GHBeaconQueryResponseV2 query(@RequestParam("referenceName") String chromosome,
                                            @RequestParam(value = "start", required = false) Long start,
                                            @RequestParam(value = "startMin", required = false) Long startMin,
                                            @RequestParam(value = "startMax", required = false) Long startMax,
                                            @RequestParam(value = "end", required = false) Long end,
                                            @RequestParam(value = "endMin", required = false) Long endMin,
                                            @RequestParam(value = "endMax", required = false) Long endMax,
                                            @RequestParam(value = "referenceBases") String referenceBases,
                                            @RequestParam(value = "alternateBases", required = false) String alternateBases,
                                            @RequestParam(value = "variantType", required = false) String variantType,
                                            @RequestParam(value = "assemblyId") String assemblyId,
                                            @RequestParam(value = "datasetIds", required = false) List<String> studies,
                                            @RequestParam(value = "includeDatasetResponses", required = false) String includeDatasetResponses,
                                            HttpServletResponse response) throws IOException, AnnotationMetadataNotFoundException {

        initializeQuery();
        BeaconAlleleRequestBody request = new BeaconAlleleRequestBody(chromosome, start, startMin, startMax, end, endMin, endMax, referenceBases, alternateBases, variantType, assemblyId, studies, includeDatasetResponses);
        if (assemblyId.equalsIgnoreCase("grch37")) {
            MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch37"));
        } else if (assemblyId.equalsIgnoreCase("grch38")) {
            MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch38"));

        } else {
            return new GA4GHBeaconQueryResponseV2("beaconId", "apiVersion", null, request, new BeaconError("Please enter a valid assemblyId", HttpServletResponse.SC_BAD_REQUEST), null);
        }
        String errorMessage = checkErrorHelper(request);

        if (errorMessage != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new GA4GHBeaconQueryResponseV2("beaconId", "apiVersion", null, request, new BeaconError(errorMessage, HttpServletResponse.SC_BAD_REQUEST), null);
        }

        VariantType variantType1;
        try {
            variantType1 = VariantType.valueOf(variantType);
        } catch (Exception e) {
            variantType1 = null;
        }

        System.out.println(variantType1);
        List<VariantRepositoryFilter> filters = new FilterBuilder().getBeaconFilters(start, startMin, startMax, end, endMin, endMax, referenceBases, alternateBases, variantType1, studies);

        //List<VariantMongo> variantMongoList = service.findByChromosomeAndOtherBeaconFilters(request);

        List<VariantMongo> variantMongoList = service.findbyChromosomeAndOtherBeaconFilters(chromosome, filters);

        List<DatasetAlleleResponse> datasetAlleleResponses = getDatasetAlleleResponsesHelper(variantMongoList, request);

        if (variantMongoList.size() > 0)
            return new GA4GHBeaconQueryResponseV2("beaconId", "apiversion", true, request, null, datasetAlleleResponses);

        else
            return new GA4GHBeaconQueryResponseV2("beaconId", "apiversion", false, request, null, datasetAlleleResponses);

    }

    public String checkErrorHelper(BeaconAlleleRequestBody request) {

        if (request.getStart() != null && request.getStart() < 0)
            return "please provide a positive start number";

        if (request.getEnd() != null && request.getEnd() < 0)
            return "pleaseprovide a positive end number";

        if (request.getAlternateBases() == null && request.getVariantType() == null)
            return "Either alternateBases ot variantType is required";

        return null;
    }

    public List<DatasetAlleleResponse> getDatasetAlleleResponsesHelper(List<VariantMongo> variantMongoList, BeaconAlleleRequestBody request) {

        List<DatasetAlleleResponse> datasetAllelResponses = new ArrayList<DatasetAlleleResponse>();

        if (request.getIncludeDatasetResponses() == null || request.getIncludeDatasetResponses().equalsIgnoreCase("NONE"))
            return null;

        HashSet<String> studiesPresent = new HashSet<String>();
        variantMongoList.forEach(variantMongo -> variantMongo.getSourceEntries().forEach(variantSourceEntryMongo -> studiesPresent.add(variantSourceEntryMongo.getStudyId())));

        if (request.getIncludeDatasetResponses().equalsIgnoreCase("HIT")) {
            Iterator<String> i = studiesPresent.iterator();
            while (i.hasNext()) {
                datasetAllelResponses.add(new DatasetAlleleResponse(i.next(), true));
            }

        } else if (request.getIncludeDatasetResponses().equalsIgnoreCase("Miss")) {
            if (request.getDatasetIds() != null) {
                request.getDatasetIds().forEach(study -> {
                    if (!studiesPresent.contains(study)) {
                        datasetAllelResponses.add(new DatasetAlleleResponse(study, false));
                    }
                });
            }
        } else if (request.getIncludeDatasetResponses().equalsIgnoreCase("ALL")) {
            if (request.getDatasetIds() != null) {
                request.getDatasetIds().forEach(study -> {
                    System.out.println(study);
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
