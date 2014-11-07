package uk.ac.ebi.variation.eva.lib.datastore;

import java.net.UnknownHostException;
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
            throws UnknownHostException, IllegalOpenCGACredentialsException {
        return new VariantMongoDBAdaptor(getCredentials(species));
    }
    
    public static StudyDBAdaptor getStudyDBAdaptor(String species)
            throws UnknownHostException, IllegalOpenCGACredentialsException {
        return new StudyMongoDBAdaptor(getCredentials(species));
    }
    
    public static VariantSourceDBAdaptor getVariantSourceDBAdaptor(String species)
            throws UnknownHostException, IllegalOpenCGACredentialsException {
        return new VariantSourceMongoDBAdaptor(getCredentials(species));
    }
    
    private static MongoCredentials getCredentials(String species) throws IllegalOpenCGACredentialsException {
        if (species == null || species.isEmpty()) {
            species = "hsapiens"; // Assume human by default
        }
        
//        return new MongoCredentials("localhost", 27017, "eva_" + species, "biouser", "biopass");
        return new MongoCredentials("mongodb-hxvm-var-001", 27017, "eva_" + species, "biouser", "B10p@ss");
    }
    
}
