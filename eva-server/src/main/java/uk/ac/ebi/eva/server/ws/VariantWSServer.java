/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014-2016 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.server.ws;

import io.swagger.annotations.Api;
import org.opencb.biodata.models.feature.Region;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;
import uk.ac.ebi.eva.lib.filter.FilterBuilder;
import uk.ac.ebi.eva.lib.filter.VariantEntityRepositoryFilter;
import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.server.Utils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/variants", produces = "application/json")
@Api(tags = {"variants"})
public class VariantWSServer extends EvaWSServer {

    @Autowired
    private VariantEntityRepository variantEntityRepository;

    protected static Logger logger = LoggerFactory.getLogger(FeatureWSServer.class);

    @RequestMapping(value = "/{variantId}/info", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "Retrieves the information about a variant", response = QueryResponse.class)
    public QueryResponse getVariantById(@PathVariable("variantId") String variantId,
                                        @RequestParam(name = "studies", required = false) List<String> studies,
                                        @RequestParam(name = "species") String species,
                                        @RequestParam(name = "annot-ct", required = false) List<String> consequenceType,
                                        @RequestParam(name = "maf", required = false) String maf,
                                        @RequestParam(name = "polyphen", required = false) String polyphenScore,
                                        @RequestParam(name = "sift", required = false) String siftScore,
                                        @RequestParam(name = "exclude", required = false) List<String> exclude,
                                        HttpServletResponse response)
            throws IllegalOpenCGACredentialsException, UnknownHostException, IOException {
        initializeQuery();

        if (species.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse("Please specify a species");
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));

        List<VariantEntity> variantEntities;
        Long numTotalResults;

        if (variantId.contains(":")) {
            String[] regionId = variantId.split(":");
            String alternate = (regionId.length > 3) ? regionId[3] : null;
            variantEntities = queryByCoordinatesAndAlleles(regionId[0], Integer.parseInt(regionId[1]), regionId[2],
                                                           alternate);
            numTotalResults = (long) variantEntities.size();
        } else {
            List<VariantEntityRepositoryFilter> filters = new FilterBuilder()
                    .getVariantEntityRepositoryFilters(maf, polyphenScore, siftScore, studies, consequenceType,
                                                       null, null, null);

            List<String> excludeMapped = new ArrayList<>();
            if (exclude != null && !exclude.isEmpty()){
                for (String e : exclude) {
                    String docPath = Utils.getApiToMongoDocNameMap().get(e);
                    if (docPath == null) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return setQueryResponse("Unrecognised exclude field: " + e);
                    }
                    excludeMapped.add(docPath);
                }
            }

            variantEntities = variantEntityRepository.findByIdsAndComplexFilters(variantId, filters, excludeMapped,
                                                                                 Utils.getPageRequest(queryOptions));

            numTotalResults = variantEntityRepository.countByIdsAndComplexFilters(variantId, filters);
        }

        QueryResult<VariantEntity> queryResult = buildQueryResult(variantEntities, numTotalResults);
        return setQueryResponse(queryResult);
    }

    private List<VariantEntity> queryByCoordinatesAndAlleles(String chromosome, int start, String reference, String alternate) {
        if (alternate != null) {
            return variantEntityRepository.findByChromosomeAndStartAndReferenceAndAlternate(chromosome, start,
                                                                                            reference, alternate);
        } else {
            return variantEntityRepository.findByChromosomeAndStartAndReference(chromosome, start, reference);
        }
    }

    @RequestMapping(value = "/{variantId}/exists", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "Checks if a variants exist", response = QueryResponse.class)
    public QueryResponse checkVariantExists(@PathVariable("variantId") String variantId,
                                            @RequestParam(name = "studies", required = false) List<String> studies,
                                            @RequestParam("species") String species,
                                            HttpServletResponse response)
            throws IllegalOpenCGACredentialsException, UnknownHostException, IOException {
        initializeQuery();

        VariantDBAdaptor variantMongoDbAdaptor = dbAdaptorConnector.getVariantDBAdaptor(species);

        if (studies != null && !studies.isEmpty()) {
            queryOptions.put("studies", studies);
        }

        String invalidCoordinatesMessage =
                "Invalid position and alleles combination, please use chr:pos:ref or chr:pos:ref:alt";

        if (!variantId.contains(":")) { // Query by accession id
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setErrorQueryResponse(invalidCoordinatesMessage);
        } else { // Query by chr:pos:ref:alt
            String parts[] = variantId.split(":", -1);
            if (parts.length < 3) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return setErrorQueryResponse(invalidCoordinatesMessage);
            }

            Region region = new Region(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[1]));
            queryOptions.put("reference", parts[2]);
            if (parts.length > 3) {
                queryOptions.put("alternate", parts[3]);
            }

            QueryResult queryResult = variantMongoDbAdaptor.getAllVariantsByRegion(region, queryOptions);
            queryResult.setResult(Arrays.asList(queryResult.getNumResults() > 0));
            queryResult.setResultType(Boolean.class.getCanonicalName());
            return setQueryResponse(queryResult);
        }
    }

}
