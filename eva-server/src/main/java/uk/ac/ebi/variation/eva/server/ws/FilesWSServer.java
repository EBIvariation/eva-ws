/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014, 2015 EMBL - European Bioinformatics Institute
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

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantSourceDBAdaptor;
import uk.ac.ebi.variation.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.variation.eva.lib.storage.metadata.VariantSourceEvaproDBAdaptor;
import uk.ac.ebi.variation.eva.server.exception.SpeciesException;
import uk.ac.ebi.variation.eva.server.exception.VersionException;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Path("/v1/files")
@Produces("application/json")
@Api(tags = { "files" })
public class FilesWSServer extends EvaWSServer {

    private final VariantSourceDBAdaptor variantSourceEvaproDbAdaptor;


    public FilesWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws NamingException, IOException {
        super(uriInfo, hsr);
        variantSourceEvaproDbAdaptor = new VariantSourceEvaproDBAdaptor();
    }

    @GET
    @Path("/all")
//    @ApiOperation(httpMethod = "GET", value = "Gets the files of a species")
    public Response getFiles(@QueryParam("species") String species) 
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        try {
            checkParams();
        } catch (VersionException | SpeciesException ex) {
            return createErrorResponse(ex.toString());
        }
        
        VariantSourceDBAdaptor variantSourceMongoDbAdaptor = DBAdaptorConnector.getVariantSourceDBAdaptor(species);
        return createOkResponse(variantSourceMongoDbAdaptor.getAllSources(queryOptions));
    }

    @GET
    @Path("/{files}/url")
//    @ApiOperation(httpMethod = "GET", value = "Gets the URL of a file")
    public Response getFileUrl(@PathParam("files") String filenames) {
        try {
            checkParams();
        } catch (VersionException | SpeciesException ex) {
            return createErrorResponse(ex.toString());
        }
        
        return createOkResponse(variantSourceEvaproDbAdaptor.getSourceDownloadUrlByName(Arrays.asList(filenames.split(","))));
    }

}
