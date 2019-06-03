package uk.ac.ebi.eva.server.ws.ga4gh;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.VariantType;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantMongo;
import uk.ac.ebi.eva.commons.mongodb.filter.FilterBuilder;
import uk.ac.ebi.eva.commons.mongodb.filter.VariantRepositoryFilter;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GA4GHBeaconWSServerV2Test {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private VariantWithSamplesAndAnnotationsService service;

    @Before
    public void setup() throws Exception {
        VariantMongo variantMongo = new VariantMongo(null,"X",100470026,100470026,1,"G","A");
        List<VariantMongo> variantMongoList = Collections.singletonList(variantMongo);
        List<VariantRepositoryFilter> variantRepositoryFilters = new FilterBuilder().getBeaconFilters(
                new Region(null,new Long(100470026),new Long(100470026)),
                new Region(null,new Long(100470026),new Long(100470026)),
                "G","A", VariantType.SNV, Arrays.asList("PRJEB7218"));

        given(service.findbyChromosomeAndOtherBeaconFilters(eq("X"), eq(variantRepositoryFilters))).willReturn(variantMongoList);
    }

    @Test
    public void testForExisting() throws Exception {
        BeaconAlleleRequestBody request = new BeaconAlleleRequestBody();
        request.setReferenceName("X");
        request.setAssemblyId("GRCh37");
        request.setReferenceBases("G");
        request.setStart(new Long(100470026));
        request.setEnd(new Long(100470026));
        request.setAlternateBases("A");
        request.setVariantType("SNV");
        request.setDatasetIds(Arrays.asList("PRJEB7218"));
        String url = String.format("/v2/beacon/query?referenceName=%s&referenceBases=%s&assemblyId=%s&alternateBases=%s&start=%s&end=%s&variantType=%s&datasetIds=%s",
                request.getReferenceName(),
                request.getReferenceBases(),
                request.getAssemblyId(),
                request.getAlternateBases(),
                request.getStart(),
                request.getEnd(),
                request.getVariantType(),
                String.join(",",request.getDatasetIds()));

        assertEquals(true,testBeaconHelper(url));

        request.setStartMin(new Long(1));
        request.setStartMax(new Long(1));
        request.setEndMin(new Long(1));
        request.setEndMax(new Long(1));
        url = String.format("/v2/beacon/query?referenceName=%s&referenceBases=%s&assemblyId=%s&alternateBases=%s&start=%s&end=%s&startMin=%s&endMin=%s&startMax=%s&endMax=%s&variantType=%s&datasetIds=%s",
                request.getReferenceName(),
                request.getReferenceBases(),
                request.getAssemblyId(),
                request.getAlternateBases(),
                request.getStart(),
                request.getEnd(),
                request.getStartMin(),
                request.getEndMin(),
                request.getStartMax(),
                request.getEndMax(),
                request.getVariantType(),
                String.join(",", request.getDatasetIds()));
        assertEquals(true,testBeaconHelper(url));
    }

    @Test
    public void testForNonExisting() {
        BeaconAlleleRequestBody request = new BeaconAlleleRequestBody();
        request.setReferenceName("Y");
        request.setAssemblyId("GRCh37");
        request.setReferenceBases("G");
        request.setAlternateBases("A");
        String url = String.format("/v2/beacon/query?referenceName=%s&referenceBases=%s&assemblyId=%s&alternateBases=%s",
                request.getReferenceName(),
                request.getReferenceBases(),
                request.getAssemblyId(),
                request.getAlternateBases());

        assertEquals(false,testBeaconHelper(url));
    }

    private boolean testBeaconHelper(String url){

        System.out.println(url);
        ResponseEntity<GA4GHBeaconQueryResponseV2> response = restTemplate.getForEntity(
                url, GA4GHBeaconQueryResponseV2.class);
        System.out.println(response.getStatusCode());
        System.out.println(response.getBody().getExists());
        return response.getBody().getExists();
    }
}
