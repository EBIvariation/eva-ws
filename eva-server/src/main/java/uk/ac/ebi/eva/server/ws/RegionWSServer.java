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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;
import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.utils.MultiMongoDbFactory;
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
                                             @RequestParam(name = "maf", defaultValue = "") String maf,
                                             @RequestParam(name = "polyphen", defaultValue = "") String polyphenScore,
                                             @RequestParam(name = "sift", defaultValue = "") String siftScore,
                                             HttpServletResponse response)
            throws IllegalOpenCGACredentialsException, IOException {
        initializeQueryOptions();

        if (species == null || species.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse("Please specify a species");
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));

        VariantEntityRepository.RelationalOperator mafOperator = VariantEntityRepository.RelationalOperator.NONE;
        Double mafvalue = null;
        if (maf != null && !maf.isEmpty()) {
            mafOperator = Utils.getRelationalOperatorFromRelation(maf);
            mafvalue = Utils.getValueFromRelation(maf);
        }

        VariantEntityRepository.RelationalOperator polyphenScoreOperator =
                VariantEntityRepository.RelationalOperator.NONE;
        Double polyphenScoreValue = null;
        if (polyphenScore != null && !polyphenScore.isEmpty()) {
            polyphenScoreOperator = Utils.getRelationalOperatorFromRelation(polyphenScore);
            polyphenScoreValue = Utils.getValueFromRelation(polyphenScore);
        }

        VariantEntityRepository.RelationalOperator siftScoreOperator = VariantEntityRepository.RelationalOperator.NONE;
        Double siftScoreValue = null;
        if (siftScore != null && !siftScore.isEmpty()) {
            siftScoreOperator = Utils.getRelationalOperatorFromRelation(siftScore);
            siftScoreValue = Utils.getValueFromRelation(siftScore);
        }

        List<Region> regions = new ArrayList<>();
        for (String s : regionId.split(",")) {
            Region r = Region.parseRegion(s);
            regions.add(r);
        }

        List<VariantEntity> variantEntities;

        if (regions.size() > 1) {
            variantEntities =
                    variantEntityRepository.findByRegionsAndComplexFilters(regions, studies, consequenceType,
                                                                           mafOperator, mafvalue, polyphenScoreOperator,
                                                                           polyphenScoreValue, siftScoreOperator,
                                                                           siftScoreValue, null);
        } else if (regions.size() == 1) {
            variantEntities =
                    variantEntityRepository.findByRegionsAndComplexFilters(regions, studies, consequenceType,
                                                                           mafOperator, mafvalue, polyphenScoreOperator,
                                                                           polyphenScoreValue, siftScoreOperator,
                                                                           siftScoreValue, null);
        } else {
            throw new IllegalArgumentException();
        }

        QueryResult<VariantEntity> queryResult = new QueryResult<>();
        queryResult.setResult(variantEntities);
        return setQueryResponse(queryResult);
    }

    @RequestMapping(value = "/{regionId}/variants", method = RequestMethod.OPTIONS)
    public QueryResponse getVariantsByRegion() {
        return setQueryResponse("");
    }
}
