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

import java.io.IOException;
import java.util.Arrays;

import javax.naming.NamingException;

import org.opencb.datastore.core.QueryResponse;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantSourceDBAdaptor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import uk.ac.ebi.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.storage.metadata.VariantSourceEvaproDBAdaptor;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@RestController
@RequestMapping(value = "/v1/files", produces = "application/json")
@Api(tags = { "files" })
public class FilesWSServer extends EvaWSServer {

    private final VariantSourceDBAdaptor variantSourceEvaproDbAdaptor;

    public FilesWSServer() throws NamingException, IOException {
        variantSourceEvaproDbAdaptor = new VariantSourceEvaproDBAdaptor();
        this.startTime = System.currentTimeMillis();
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "Gets the files of a species")
    public QueryResponse getFiles(@RequestParam("species") String species) 
            throws IllegalOpenCGACredentialsException, IOException {
        initializeQueryOptions();
        
        VariantSourceDBAdaptor variantSourceMongoDbAdaptor = DBAdaptorConnector.getVariantSourceDBAdaptor(species);
        return setQueryResponse(variantSourceMongoDbAdaptor.getAllSources(queryOptions));
    }

    @RequestMapping(value = "/{files}/url", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "Gets the URL of a file")
    public QueryResponse getFileUrl(@PathVariable("files") String filenames) {
        initializeQueryOptions();
        return setQueryResponse(variantSourceEvaproDbAdaptor.getSourceDownloadUrlByName(Arrays.asList(filenames.split(","))));
    }

}
