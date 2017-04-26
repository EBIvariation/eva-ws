package uk.ac.ebi.eva.lib.datastore;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.ebi.eva.lib.MongoConfiguration;
import uk.ac.ebi.eva.lib.MultiMongoFactoryConfiguration;
import uk.ac.ebi.eva.lib.configuration.SpringDataMongoDbProperties;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.utils.MultiMongoDbFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfiguration.class, MultiMongoFactoryConfiguration.class})
@SpringBootTest
@EnableConfigurationProperties
public class DBAdaptorConnectorTest {

    @Autowired
    private MongoDbFactory factory;

    @Autowired
    private SpringDataMongoDbProperties springDataMongoDbProperties;

    @Test
    public void testSpringDataMongoDbPropertiesAutowiring() {
        assertNotNull(springDataMongoDbProperties);
        assertNotNull(springDataMongoDbProperties.getHost());
    }

    /**
     * Check that spring is autowiring our MultiMongoDbFactory as the MongoDbFactory to use.
     *
     * To check it, we use MultiMongoDbFactory::setDatabaseNameForCurrentThread to change the DB we should get later
     * when we do a `factory.getDb()`
     */
    @Test
    public void testMongoDbFactoryAutowiring() {
        String dbName = "DBAdaptorConnectorTest_testMongoDbFactoryAutowiring";
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(dbName);
        DB db = factory.getDb();
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
