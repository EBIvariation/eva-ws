package uk.ac.ebi.variation.eva.server.ws;

import java.io.IOException;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.opencb.opencga.storage.variant.ArchiveDBAdaptor;
import uk.ac.ebi.variation.eva.lib.storage.metadata.ArchiveEvaproDBAdaptor;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Path("/{version}/meta")
@Produces(MediaType.APPLICATION_JSON)
public class ArchiveWSServer extends EvaWSServer {
    
    private ArchiveDBAdaptor dbAdaptor;
    
    public ArchiveWSServer() {

    }

    public ArchiveWSServer(@DefaultValue("") @PathParam("version")String version, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) 
            throws IOException, NamingException {
        super(version, uriInfo, hsr);
        dbAdaptor = new ArchiveEvaproDBAdaptor();
    }

    @GET
    @Path("/files/count")
    public Response countFiles() {
        return createOkResponse(dbAdaptor.countFiles());
    }
    
    @GET
    @Path("/species/count")
    public Response countSpecies() {
        return createOkResponse(dbAdaptor.countSpecies());
    }
    
    @GET
    @Path("/studies/count")
    public Response countStudies() {
        return createOkResponse(dbAdaptor.countStudies());
    }
    
}
