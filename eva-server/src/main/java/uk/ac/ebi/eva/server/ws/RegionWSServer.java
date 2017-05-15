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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;
import uk.ac.ebi.eva.lib.filter.FilterBuilder;
import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.lib.filter.VariantEntityRepositoryFilter;
import uk.ac.ebi.eva.server.Utils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/segments", produces = "application/json")
@Api(tags = { "segments" })
public class RegionWSServer extends EvaWSServer {

    @Autowired
    private VariantEntityRepository variantEntityRepository;

    protected static Logger logger = LoggerFactory.getLogger(FeatureWSServer.class);

    public RegionWSServer() {
    }

    @RequestMapping(value = "/{regionId}/variants", method = RequestMethod.GET)
    @ResponseBody
//    @ApiOperation(httpMethod = "GET", value = "Retrieves all the variants from region", response = QueryResponse.class)
    public QueryResponse getVariantsByRegion(@PathVariable("regionId") String regionId,
                                             @RequestParam(name = "species") String species,
                                             @RequestParam(name = "studies", required = false) List<String> studies,
                                             @RequestParam(name = "annot-ct", required = false) List<String> consequenceType,
                                             @RequestParam(name = "maf", required = false) String maf,
                                             @RequestParam(name = "polyphen", required = false) String polyphenScore,
                                             @RequestParam(name = "sift", required = false) String siftScore,
                                             @RequestParam(name = "exclude", required = false) List<String> exclude,
                                             HttpServletResponse response)
            throws IllegalOpenCGACredentialsException, IOException {
        initializeQuery();

        if (species.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse("Please specify a species");
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));

        List<VariantEntityRepositoryFilter> filters = new FilterBuilder()
                .getVariantEntityRepositoryFilters(maf, polyphenScore, siftScore, studies, consequenceType
                );
        List<Region> regions = Region.parseRegions(regionId);
        PageRequest pageRequest = Utils.getPageRequest(queryOptions);

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

        List<VariantEntity> variantEntities =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, filters, excludeMapped, pageRequest);

        Long numTotalResults = variantEntityRepository.countByRegionsAndComplexFilters(regions, filters);

        QueryResult<VariantEntity> queryResult = buildQueryResult(variantEntities, numTotalResults);
        return setQueryResponse(queryResult);
    }

    @RequestMapping(value = "/{regionId}/variants", method = RequestMethod.OPTIONS)
    public QueryResponse getVariantsByRegion() {
        return setQueryResponse("");
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
//    @ApiOperation(httpMethod = "GET", value = "Retrieves all the variants from region", response = QueryResponse.class)
    public QueryResponse getChromosomes(@RequestParam(name = "species") String species,
                                        HttpServletResponse response)
            throws IllegalOpenCGACredentialsException, IOException {
        if (species.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse("Please specify a species");
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));
        List<String> chromosomeList = new ArrayList<>(variantEntityRepository.findDistinctChromosomes());
        QueryResult<String> queryResult = buildQueryResult(chromosomeList);
        return setQueryResponse(queryResult);
    }
}
