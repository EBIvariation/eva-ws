package uk.ac.ebi.variation.eva.lib.datastore;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.lib.auth.MongoCredentials;
import org.opencb.opencga.storage.variant.StudyDBAdaptor;
import org.opencb.opencga.storage.variant.VariantDBAdaptor;
import org.opencb.opencga.storage.variant.VariantSourceDBAdaptor;
import org.opencb.opencga.storage.variant.mongodb.StudyMongoDBAdaptor;
import org.opencb.opencga.storage.variant.mongodb.VariantMongoDBAdaptor;
import org.opencb.opencga.storage.variant.mongodb.VariantSourceMongoDBAdaptor;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class DBAdaptorConnector {
    
    public static VariantDBAdaptor getVariantDBAdaptor(String species) 
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        return new VariantMongoDBAdaptor(getCredentials(species));
    }
    
    public static StudyDBAdaptor getStudyDBAdaptor(String species)
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        return new StudyMongoDBAdaptor(getCredentials(species));
    }
    
    public static VariantSourceDBAdaptor getVariantSourceDBAdaptor(String species)
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        return new VariantSourceMongoDBAdaptor(getCredentials(species));
    }
    
    private static MongoCredentials getCredentials(String species) throws IllegalOpenCGACredentialsException, IOException {
        if (species == null || species.isEmpty()) {
            species = "hsapiens"; // Assume human by default
        }
        
        Properties properties = new Properties(); 
        properties.load(DBAdaptorConnector.class.getResourceAsStream("/mongo.properties"));
        
        return new MongoCredentials(properties.getProperty("eva.mongo.host"),
                Integer.parseInt(properties.getProperty("eva.mongo.port")),
                "eva_" + species,
                properties.getProperty("eva.mongo.user"),
                properties.getProperty("eva.mongo.passwd"));
    }
    
}
