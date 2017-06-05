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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.eva.commons.mongodb.services.VariantSourceService;
import uk.ac.ebi.eva.lib.metadata.eva.VariantSourceEvaProDBAdaptor;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.lib.utils.QueryUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@RestController
@RequestMapping(value = "/v1/files", produces = "application/json")
@Api(tags = {"files"})
public class FilesWSServer extends EvaWSServer {

    @Autowired
    private VariantSourceEvaProDBAdaptor variantSourceEvaproDbAdaptor;

    @Autowired
    private VariantSourceService service;

    @Autowired
    private QueryUtils queryUtils;

    @RequestMapping(value = "/all", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "Gets the files of a species")
    public QueryResponse getFiles(@RequestParam("species") String species)
            throws IOException {
        queryUtils.initializeQuery();

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));
        return queryUtils.setQueryResponse(queryUtils.buildQueryResult(service.findAll()));
    }

    @RequestMapping(value = "/{files}/url", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "Gets the URL of a file")
    public QueryResponse getFileUrl(@PathVariable("files") String filenames) {
        initializeQuery();
        return setQueryResponse(variantSourceEvaproDbAdaptor.getSourceDownloadUrlByName(Arrays.asList(filenames.split(","))));
    }

}
