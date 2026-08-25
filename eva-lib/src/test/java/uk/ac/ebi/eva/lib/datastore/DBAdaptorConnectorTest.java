package uk.ac.ebi.eva.lib.datastore;


import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.eva.lib.MongoConfiguration;
import uk.ac.ebi.eva.lib.MultiMongoFactoryConfiguration;
import uk.ac.ebi.eva.lib.configuration.MongoRepositoryTestConfiguration;
import uk.ac.ebi.eva.lib.configuration.SpringDataMongoDbProperties;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MongoConfiguration.class, MultiMongoFactoryConfiguration.class,
        MongoRepositoryTestConfiguration.class})
@SpringBootTest
@EnableConfigurationProperties
public class DBAdaptorConnectorTest {
    @Autowired
    private MongoDatabaseFactory factory;

    @Autowired
    private SpringDataMongoDbProperties springDataMongoDbProperties;

    @Test
    public void testSpringDataMongoDbPropertiesAutowiring() {
        assertNotNull(springDataMongoDbProperties);
        assertNotNull(springDataMongoDbProperties.getHost());
    }

    /**
     * Check that spring is autowiring our MultiMongoDbFactory as the MongoDbFactory to use.
     * <p>
     * To check it, we use MultiMongoDbFactory::setDatabaseNameForCurrentThread to change the DB we should get later
     * when we do a `factory.getDb()`
     */
    @Test
    public void testMongoDbFactoryAutowiring() {
        String dbName = "test-db";
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(dbName);
        MongoDatabase db = factory.getMongoDatabase();
        assertEquals(db.getName(), dbName);
    }

    /**
     * Check that the value secondaryPreferred is used when it's not specified in the properties.
     *
     * @throws Exception
     */
    @Test
    public void testDefaultReadPreferenceInMongoClientEvaProperty() throws Exception {
        MongoClient mongoClient = DBAdaptorConnector.getMongoClient(springDataMongoDbProperties);
        assertEquals(ReadPreference.secondaryPreferred(), mongoClient.getReadPreference());
    }
}
