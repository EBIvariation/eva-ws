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

package uk.ac.ebi.eva.lib.datastore;

import com.mongodb.*;
import org.opencb.datastore.core.config.DataStoreServerAddress;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.lib.common.*;
import org.opencb.opencga.storage.core.adaptors.StudyDBAdaptor;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
import org.opencb.opencga.storage.core.variant.adaptors.VariantSourceDBAdaptor;
import org.opencb.opencga.storage.mongodb.utils.MongoCredentials;
import org.opencb.opencga.storage.mongodb.variant.StudyMongoDBAdaptor;
import org.opencb.opencga.storage.mongodb.variant.VariantMongoDBAdaptor;
import org.opencb.opencga.storage.mongodb.variant.VariantSourceMongoDBAdaptor;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class DBAdaptorConnector {

    public static VariantDBAdaptor getVariantDBAdaptor(String species) 
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        Properties properties = new Properties(); 
        properties.load(DBAdaptorConnector.class.getResourceAsStream("/eva.properties"));
        return new VariantMongoDBAdaptor(getCredentials(species, properties),
                properties.getProperty("eva.mongo.collections.variants"), 
                properties.getProperty("eva.mongo.collections.files"));
    }
    
    public static StudyDBAdaptor getStudyDBAdaptor(String species)
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        Properties properties = new Properties(); 
        properties.load(DBAdaptorConnector.class.getResourceAsStream("/eva.properties"));
        return new StudyMongoDBAdaptor(getCredentials(species, properties),
                properties.getProperty("eva.mongo.collections.files"));
    }
    
    public static VariantSourceDBAdaptor getVariantSourceDBAdaptor(String species)
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        Properties properties = new Properties(); 
        properties.load(DBAdaptorConnector.class.getResourceAsStream("/eva.properties"));
        return new VariantSourceMongoDBAdaptor(getCredentials(species, properties),
                properties.getProperty("eva.mongo.collections.files"));
    }

    /**
     * Get a MongoClient using the configuration (credentials) in a given Properties.
     *
     * @param properties can have the next values:
     *                   - eva.mongo.auth.db authentication database
     *                   - eva.mongo.host comma-separated strings of colon-separated host and port strings: host_1:port_1,host_2:port_2
     *                   - eva.mongo.user
     *                   - eva.mongo.passwd
     *                   - eva.mongo.read-preference string, "secondaryPreferred" if unspecified. one of:
     *                          [primary, primaryPreferred, secondary, secondaryPreferred, nearest]
     * @return MongoClient with given credentials
     * @throws UnknownHostException
     */
    public static MongoClient getMongoClient(Properties properties) throws UnknownHostException {

        String[] hosts = properties.getProperty("eva.mongo.host").split(",");
        List<ServerAddress> servers = new ArrayList<>();

        // Get the list of hosts (optionally including the port number)
        for (String host : hosts) {
            String[] params = host.split(":");
            if (params.length > 1) {
                servers.add(new ServerAddress(params[0], Integer.parseInt(params[1])));
            } else {
                servers.add(new ServerAddress(params[0], 27017));
            }
        }

        List<MongoCredential> mongoCredentialList = null;
        String authenticationDb = properties.getProperty("eva.mongo.auth.db", null);
        if (authenticationDb != null && !authenticationDb.isEmpty()) {
            mongoCredentialList = Collections.singletonList(MongoCredential.createCredential(
                    properties.getProperty("eva.mongo.user"),
                    authenticationDb,
                    properties.getProperty("eva.mongo.passwd").toCharArray()));
        }


        String readPreference = properties.getProperty("eva.mongo.read-preference");
        readPreference = readPreference == null || readPreference.isEmpty()? "secondaryPreferred" : readPreference;

        MongoClientOptions options = MongoClientOptions.builder()
                .readPreference(ReadPreference.valueOf(readPreference))
                .build();

        return new MongoClient(servers, mongoCredentialList, options);
    }

    /**
     * Extract org.opencb.opencga.storage.mongodb.utils.MongoCredentials from a given Properties to a given species.
     *
     * @param properties can have the next values:
     *                   - eva.mongo.auth.db authentication database
     *                   - eva.mongo.host comma-separated strings of colon-separated host and port strings: host_1:port_1,host_2:port_2
     *                   - eva.mongo.user
     *                   - eva.mongo.passwd
     * @return org.opencb.opencga.storage.mongodb.utils.MongoCredentials
     * @throws UnknownHostException
     */
    private static MongoCredentials getCredentials(String species, Properties properties)
            throws IllegalOpenCGACredentialsException, IOException {
        if (species == null || species.isEmpty()) {
            throw new IllegalArgumentException("Please specify a species");
        }
        
        String[] hosts = properties.getProperty("eva.mongo.host").split(",");
        List<DataStoreServerAddress> servers = new ArrayList();
        
        // Get the list of hosts (optionally including the port number)
        for (String host : hosts) {
            String[] params = host.split(":");
            if (params.length > 1) {
                servers.add(new DataStoreServerAddress(params[0], Integer.parseInt(params[1])));
            } else {
                servers.add(new DataStoreServerAddress(params[0], 27017));
            }
        }
        
        MongoCredentials credentials = new MongoCredentials(servers,
                getDBName(species),
                properties.getProperty("eva.mongo.user"),
                properties.getProperty("eva.mongo.passwd"));
        
        // Set authentication database, if specified in the configuration
        credentials.setAuthenticationDatabase(properties.getProperty("eva.mongo.auth.db", null));
        
        return credentials;
    }

    public static String getDBName(String species) {
        return "eva_" + species;
    }
}
