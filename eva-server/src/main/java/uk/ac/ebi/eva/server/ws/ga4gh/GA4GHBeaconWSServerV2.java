package uk.ac.ebi.eva.server.ws.ga4gh;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.eva.commons.core.utils.BeaconAllelRequest;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantMongo;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantSourceMongo;
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

    /*@RequestMapping(value = "/" , method = RequestMethod.GET)
    public List<VariantSourceMongo>  getDetails()
    {

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch37"));

       List<VariantSourceMongo> variantSourceMongos= repository.findAll();
       return variantSourceMongos;


        //GA4GHBeaconResponseV2 v2 = new GA4GHBeaconResponseV2();
        //return v2;
    }*/

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public GA4GHBeaconQueryResponseV2 query(@RequestParam("referenceName") String chromosome,
                                            @RequestParam(value = "start", required = false) Long start,
                                            @RequestParam(value = "startMin", required = false) String startMin,
                                            @RequestParam(value = "startMax", required = false) String startMax,
                                            @RequestParam(value = "end", required = false) Long end,
                                            @RequestParam(value = "endMin", required = false) String endMin,
                                            @RequestParam(value = "endMax", required = false) String endMax,
                                            @RequestParam(value = "referenceBases") String referenceBases,
                                            @RequestParam(value = "alternateBases", required = false) String alternateBases,
                                            @RequestParam(value = "variantType", required = false) String variantType,
                                            @RequestParam(value = "assemblyId") String assemblyId,
                                            @RequestParam(value = "datasetIds", required = false) List<String> studies,
                                            @RequestParam(value = "includeDatasetResponses", required = false) String includeDatasetResponses,
                                            HttpServletResponse response) throws IOException, AnnotationMetadataNotFoundException {

        initializeQuery();
        BeaconAllelRequest request = new BeaconAllelRequest(chromosome, start, startMin, startMax, end, endMin, endMax, referenceBases, alternateBases, variantType, assemblyId, studies, includeDatasetResponses);
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch37"));

        String errorMessage = checkErrorHelper(request);

        if (errorMessage != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new GA4GHBeaconQueryResponseV2("beaconId", "apiVersion", null, request, new BeaconError(errorMessage, HttpServletResponse.SC_BAD_REQUEST), null);
        }

        List<VariantMongo> variantMongoList = service.findByChromosomeAndOtherBeaconFilters(request);
        List<VariantSourceMongo> filterByAssemblyId = sourceService.findbyAssemblyId(assemblyId);

        List<DatasetAllelResponse> datasetAllelResponses = getDatasetAllelResponsesHelper(variantMongoList, request);

        if (variantMongoList.size() > 0)
            return new GA4GHBeaconQueryResponseV2("beaconId", "apiversion", true, request, null, datasetAllelResponses);

        else
            return new GA4GHBeaconQueryResponseV2("beaconId", "apiversion", false, request, null, datasetAllelResponses);

    }

    public String checkErrorHelper(BeaconAllelRequest request) {

        if (request.getStart() != null && request.getStart() < 0)
            return "please provide a positive start number";

        if (request.getEnd() != null && request.getEnd() < 0)
            return "pleaseprovide a positive end number";

        if (request.getAlternateBases() == null && request.getVariantType() == null)
            return "Either alternateBases ot variantType is required";

        return null;
    }

    public List<DatasetAllelResponse> getDatasetAllelResponsesHelper(List<VariantMongo> variantMongoList, BeaconAllelRequest request) {

        List<DatasetAllelResponse> datasetAllelResponses = new ArrayList<DatasetAllelResponse>();

        if (request.getIncludeDatasetResponses() == null || request.getIncludeDatasetResponses().equalsIgnoreCase("NONE"))
            return null;

        HashSet<String> studiesPresent = new HashSet<String>();
        variantMongoList.forEach(variantMongo -> variantMongo.getSourceEntries().forEach(variantSourceEntryMongo -> studiesPresent.add(variantSourceEntryMongo.getStudyId())));

        if (request.getIncludeDatasetResponses().equalsIgnoreCase("HIT")) {
            Iterator<String> i = studiesPresent.iterator();
            while (i.hasNext()) {
                datasetAllelResponses.add(new DatasetAllelResponse(i.next(), true));
            }

        } else if (request.getIncludeDatasetResponses().equalsIgnoreCase("Miss")) {
            if (request.getStudies() != null) {
                request.getStudies().forEach(study -> {
                    if (!studiesPresent.contains(study)) {
                        datasetAllelResponses.add(new DatasetAllelResponse(study, false));
                    }
                });
            }
        } else if (request.getIncludeDatasetResponses().equalsIgnoreCase("ALL")) {
            if (request.getStudies() != null) {
                request.getStudies().forEach(study -> {
                    System.out.println(study);
                    if (!studiesPresent.contains(study)) {
                        datasetAllelResponses.add(new DatasetAllelResponse(study, false));
                    } else {
                        datasetAllelResponses.add(new DatasetAllelResponse(study, true));
                    }
                });
            }
        }

        return datasetAllelResponses;
    }


}
