package uk.ac.ebi.variation.eva.server.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Splitter;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.io.IOException;

/**
 * Created by imedina on 01/04/14.
 */
@Path("/{version}")
//@Produces(MediaType.APPLICATION_JSON)
public class EvaWSServer {

    // output content format: txt or text, json, xml, das
    protected String outputFormat;

    protected String version;
    protected UriInfo uriInfo;
    protected HttpServletRequest httpServletRequest;

    protected QueryOptions queryOptions;
    protected QueryResponse queryResponse;
    protected long startTime;
    protected long endTime;

    protected static ObjectMapper jsonObjectMapper;
    protected static ObjectWriter jsonObjectWriter;
    protected static XmlMapper xmlObjectMapper;

    protected static Logger logger;


    static {
//        BasicConfigurator.configure();
//        dbAdaptorFactory = new HibernateDBAdaptorFactory();

        logger = LoggerFactory.getLogger(EvaWSServer.class);

        jsonObjectMapper = new ObjectMapper();
        jsonObjectWriter = jsonObjectMapper.writer();

        xmlObjectMapper = new XmlMapper();

        logger.info("EvaWSServer: Initiating attributes inside static block");
    }

    public EvaWSServer() {
        logger.info("EvaWSServer: in 'constructor'");
    }

    public EvaWSServer(@PathParam("version") String version,
                        @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws IOException {


        this.version = version;
        this.uriInfo = uriInfo;
        this.httpServletRequest = hsr;

        init(version, uriInfo);

        logger.info("EvaWSServer: in 'constructor'");

        // if(version != null && species != null) {
        // }
    }

    protected void init(String version, UriInfo uriInfo) throws IOException {

        startTime = System.currentTimeMillis();
        queryResponse = new QueryResponse();


        // load properties file
        // ResourceBundle databaseConfig =
        // ResourceBundle.getBundle("org.bioinfo.infrared.ws.application");
        // config = new Config(databaseConfig);


        // mediaType = MediaType.valueOf("text/plain");
        queryOptions = new QueryOptions();
        // logger = new Logger();
        // logger.setLevel(Logger.DEBUG_LEVEL);
        logger.info("GenericrestWSServer: in 'init' method");

        /**
         * Check version parameter, must be: v1, v2, ... If 'latest' then is
         * converted to appropriate version
         */
        // if(version != null && version.equals("latest") &&
        // config.getProperty("CELLBASE.LATEST.VERSION") != null) {
        // version = config.getProperty("CELLBASE.LATEST.VERSION");
        // System.out.println("version init: "+version);
        // }

        // this code MUST be run before the checking
        parseCommonQueryParameters(uriInfo.getQueryParameters());
    }

    private void parseCommonQueryParameters(MultivaluedMap<String, String> multivaluedMap) {
        queryOptions.put("metadata", (multivaluedMap.get("metadata") != null) ? multivaluedMap.get("metadata").get(0).equals("true") : true);
        queryOptions.put("exclude", (multivaluedMap.get("exclude") != null) ? Splitter.on(",").splitToList(multivaluedMap.get("exclude").get(0)) : null);
        queryOptions.put("include", (multivaluedMap.get("include") != null) ? Splitter.on(",").splitToList(multivaluedMap.get("include").get(0)) : null);
        queryOptions.put("limit", (multivaluedMap.get("limit") != null) ? multivaluedMap.get("limit").get(0) : -1);
        queryOptions.put("skip", (multivaluedMap.get("skip") != null) ? multivaluedMap.get("skip").get(0) : -1);
        queryOptions.put("count", (multivaluedMap.get("count") != null) ? Boolean.parseBoolean(multivaluedMap.get("count").get(0)) : false);

        outputFormat = (multivaluedMap.get("of") != null) ? multivaluedMap.get("of").get(0) : "json";
//        outputCompress = (multivaluedMap.get("outputcompress") != null) ? multivaluedMap.get("outputcompress").get(0) : "false";
    }
    @GET
    @Path("/test")
    public Response help() {
        return createOkResponse("No help available");
    }



    protected Response createOkResponse(Object obj) {
        endTime = System.currentTimeMillis() - startTime;
        queryResponse.put("time", endTime);
        queryResponse.put("version", version);
        queryResponse.put("queryOptions", queryOptions);

        switch (outputFormat.toLowerCase()) {
            case "json":
                return createJsonResponse(obj);
            case "xml":
                return createXmlResponse(obj);
            default:
                return buildResponse(Response.ok(obj));
        }
    }

    protected Response createOkResponse(Object obj, MediaType mediaType) {
        return buildResponse(Response.ok(obj, mediaType));
    }

    protected Response createOkResponse(Object obj, MediaType mediaType, String fileName) {
        return buildResponse(Response.ok(obj, mediaType).header("content-disposition", "attachment; filename =" + fileName));
    }


    protected Response createJsonResponse(Object obj) {
        queryResponse.put("response", obj);
        try {
            return buildResponse(Response.ok(jsonObjectWriter.writeValueAsString(queryResponse), MediaType.APPLICATION_JSON_TYPE));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Error parsing queryResponse object");
            return null;
        }
    }

    protected Response createXmlResponse(Object obj) {
        queryResponse.put("response", obj);
        try {
//            return buildResponse(Response.ok(jsonObjectWriter.writeValueAsString(queryResponse), MediaType.APPLICATION_JSON_TYPE));
            return buildResponse(Response.ok(xmlObjectMapper.writeValueAsString(queryResponse), MediaType.APPLICATION_XML_TYPE));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Error parsing queryResponse object");
            return null;
        }
    }


    private Response buildResponse(Response.ResponseBuilder responseBuilder) {
        return responseBuilder.header("Access-Control-Allow-Origin", "*").build();
    }
}
