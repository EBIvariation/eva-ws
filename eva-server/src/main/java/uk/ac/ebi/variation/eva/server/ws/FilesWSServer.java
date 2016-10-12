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

package uk.ac.ebi.variation.eva.server.ws;

import io.swagger.annotations.Api;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantSourceDBAdaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.variation.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.variation.eva.lib.spring.data.metadata.SpringVariantSourceEvaProDBAdaptor;

import javax.naming.NamingException;
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
    private SpringVariantSourceEvaProDBAdaptor variantSourceEvaproDbAdaptor;

    public FilesWSServer() throws NamingException, IOException {
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
