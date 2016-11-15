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
import org.opencb.biodata.models.variant.Variant;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.server.repository.VariantRepository;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.utils.MultiMongoDbFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@RestController
@RequestMapping(value = "/v1/variants", produces = "application/json")
@Api(tags = {"variants"})
@EnableMongoRepositories("uk.ac.ebi.eva.server.repository")
public class VariantWSServer extends EvaWSServer {

    @Autowired
    private VariantRepository variantRepository;

    protected static Logger logger = LoggerFactory.getLogger(VariantWSServer.class);

    @RequestMapping(value = "/{variantId}/info", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "Retrieves the information about a variant", response = QueryResponse.class)
    public QueryResponse getVariantById(@PathVariable("variantId") String variantId,
                                        @RequestParam(name = "studies", required = false) List<String> studies,
                                        @RequestParam("species") String species,
                                        HttpServletResponse response)
            throws IllegalOpenCGACredentialsException, UnknownHostException, IOException {

        initializeQueryOptions();

        if (studies != null && !studies.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            queryOptions.put("studies", studies);
        }

        if (!variantId.contains(":")) { // Query by accession id
            QueryResult<Variant> queryResult = new QueryResult<>();
            String dbName = DBAdaptorConnector.getDBName(species);
            MultiMongoDbFactory.setDatabaseNameForCurrentThread(dbName);

            List<Variant> variants = variantRepository.findByIds(variantId);
            queryResult.setResult(variants);

            return setQueryResponse(queryResult);
        } else { // Query by chr:pos:ref:alt

            VariantDBAdaptor variantMongoDbAdaptor = DBAdaptorConnector.getVariantDBAdaptor(species);

            logger.warn("variantId: " + variantId);

            String parts[] = variantId.split(":", -1);
            logger.warn("parts_0: " + parts[0]);
            logger.warn("parts_1: " + parts[1]);
            logger.warn("parts_2: " + parts[2]);
            if (parts.length < 3) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return setQueryResponse("Invalid position and alleles combination, please use chr:pos:ref or chr:pos:ref:alt");
            }

            Region region = new Region(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[1]));
            logger.warn("region: " + region);
            queryOptions.put("reference", parts[2]);
            if (parts.length > 3) {
                queryOptions.put("alternate", String.join(":", Arrays.copyOfRange(parts, 3, parts.length)));
            }

            return setQueryResponse(variantMongoDbAdaptor.getAllVariantsByRegion(region, queryOptions));

        }
    }

    @RequestMapping(value = "/{variantId}/exists", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "Checks if a variants exist", response = QueryResponse.class)
    public QueryResponse checkVariantExists(@PathVariable("variantId") String variantId,
                                            @RequestParam(name = "studies", required = false) List<String> studies,
                                            @RequestParam("species") String species,
                                            HttpServletResponse response)
            throws IllegalOpenCGACredentialsException, UnknownHostException, IOException {
        initializeQueryOptions();

        VariantDBAdaptor variantMongoDbAdaptor = DBAdaptorConnector.getVariantDBAdaptor(species);

        if (studies != null && !studies.isEmpty()) {
            queryOptions.put("studies", studies);
        }

        if (!variantId.contains(":")) { // Query by accession id
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse("Invalid position and alleles combination, please use chr:pos:ref or chr:pos:ref:alt");
        } else { // Query by chr:pos:ref:alt
            String parts[] = variantId.split(":", -1);
            if (parts.length < 3) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return setQueryResponse("Invalid position and alleles combination, please use chr:pos:ref or chr:pos:ref:alt");
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
