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

import com.mongodb.BasicDBObject;
import io.swagger.annotations.Api;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;
import uk.ac.ebi.eva.lib.filter.FilterBuilder;
import uk.ac.ebi.eva.lib.filter.VariantEntityRepositoryFilter;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.utils.MultiMongoDbFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/genes", produces = "application/json")
@Api(tags = { "genes" })
public class GeneWSServer extends EvaWSServer {

    public GeneWSServer() { }

    @RequestMapping(value = "/{geneIds}/variants", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "Retrieves all the variants of a gene", response = QueryResponse.class)
    public QueryResponse getVariantsByGene(@PathVariable("geneIds") List<String> geneIds,
                                           @RequestParam(name = "species") String species,
                                           @RequestParam(name = "studies", required = false) List<String> studies,
                                           @RequestParam(name = "annot-ct", required = false) List<String> consequenceType,
                                           @RequestParam(name = "maf", defaultValue = "") String maf,
                                           @RequestParam(name = "polyphen", defaultValue = "") String polyphenScore,
                                           @RequestParam(name = "sift", defaultValue = "") String siftScore,
                                           @RequestParam(name = "ref", defaultValue = "") String reference,
                                           @RequestParam(name = "alt", defaultValue = "") String alternate,
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

        FilterBuilder filterBuilder = new FilterBuilder();

        List<VariantEntityRepositoryFilter> filters =
                filterBuilder.getVariantEntityRepositoryFilters(maf, polyphenScore, siftScore, studies, consequenceType);
        filters.addAll(filterBuilder.withXrefsIds(geneIds).build());



        // OLD IMPLEMENTATION

        initializeQuery();

        VariantDBAdaptor variantMongoDbAdaptor = dbAdaptorConnector.getVariantDBAdaptor(species);

        if (studies != null && !studies.isEmpty()) {
            queryOptions.put(VariantDBAdaptor.STUDIES, studies);
        }

        if (consequenceType != null && !consequenceType.isEmpty()) {
            queryOptions.put(VariantDBAdaptor.ANNOT_CONSEQUENCE_TYPE, consequenceType);
        }

        if (!maf.isEmpty()) {
            queryOptions.put(VariantDBAdaptor.MAF, maf);
        }
        if (!polyphenScore.isEmpty()) {
            queryOptions.put(VariantDBAdaptor.POLYPHEN, polyphenScore);
        }
        if (!siftScore.isEmpty()) {
            queryOptions.put(VariantDBAdaptor.SIFT, siftScore);
        }

        if (!reference.isEmpty()) {
            queryOptions.put(VariantDBAdaptor.REFERENCE, reference);
        }
        if (!alternate.isEmpty()) {
            queryOptions.put(VariantDBAdaptor.ALTERNATE, alternate);
        }

        queryOptions.put(VariantDBAdaptor.SORT, new BasicDBObject("chr", 1).append("start", 1));
        queryOptions.put(VariantDBAdaptor.GENE, String.join(",", geneIds));

        return setQueryResponse(variantMongoDbAdaptor.getAllVariants(queryOptions));

    }

    @RequestMapping(value = "/{geneIds}/variants", method = RequestMethod.POST)
    public QueryResponse getVariantsByGenePOST(@PathVariable("geneIds") List<String> geneIds,
                                               @RequestParam(name = "species") String species,
                                               @RequestParam(name = "studies", required = false) List<String> studies,
                                               @RequestParam(name = "annot-ct", required = false) List<String> consequenceType,
                                               @RequestParam(name = "maf", defaultValue = "") String maf,
                                               @RequestParam(name = "polyphen", defaultValue = "") String polyphenScore,
                                               @RequestParam(name = "sift", defaultValue = "") String siftScore,
                                               @RequestParam(name = "ref", defaultValue = "") String reference,
                                               @RequestParam(name = "alt", defaultValue = "") String alternate,
                                               HttpServletResponse response)
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        return getVariantsByGene(geneIds, species, studies, consequenceType, maf, polyphenScore, siftScore,
                                 reference, alternate, response);
    }

}
