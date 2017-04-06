package uk.ac.ebi.eva.lib.datastore;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.ebi.eva.lib.MongoConfiguration;
import uk.ac.ebi.eva.lib.MultiMongoFactoryConfiguration;
import uk.ac.ebi.eva.lib.config.EvaProperty;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.utils.MultiMongoDbFactory;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfiguration.class, MultiMongoFactoryConfiguration.class, EvaProperty.class })
@SpringBootTest(classes = {EvaProperty.class})
@EnableConfigurationProperties
public class DBAdaptorConnectorTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MongoDbFactory factory;

    private Properties properties;

    @Autowired
    private EvaProperty evaProperty;

    @Test
    public void testEvaPropertyAutowiring() {
//        EvaProperty evaProperty = DBAdaptorConnector.getEvaProperty();
        assertNotNull(evaProperty);
        assertNotNull(evaProperty.getMongo());
        System.out.println(evaProperty);
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
    public void testDefaultReadPreferenceInMongoClient() throws Exception {
        MongoClient mongoClient = DBAdaptorConnector.getMongoClient(properties);
        assertEquals(ReadPreference.secondaryPreferred(), mongoClient.getReadPreference());
    }

    @Test
    public void testDefaultReadPreferenceInMongoClientEvaProperty() throws Exception {
        MongoClient mongoClient = DBAdaptorConnector.getMongoClient(evaProperty);
        assertEquals(ReadPreference.secondaryPreferred(), mongoClient.getReadPreference());
    }

    /**
     * Check that the value:
     * eva.mongo.read-preference=nearest
     * is properly read by DBAdaptorConnector::getMongoClient
     *
     * @throws Exception
     */
    @Test
    public void testReadPreferenceInMongoClient() throws Exception {
        ReadPreference readPreference = ReadPreference.nearest();
        properties.put("eva.mongo.read-preference", readPreference.getName());
        MongoClient mongoClient = DBAdaptorConnector.getMongoClient(properties);

        assertEquals(readPreference, mongoClient.getReadPreference());
    }

    @Test
    public void testReadPreferenceInMongoClientEvaProperty() throws Exception {
        ReadPreference readPreference = ReadPreference.nearest();
        evaProperty.getMongo().setReadPreference(readPreference.getName());
        MongoClient mongoClient = DBAdaptorConnector.getMongoClient(evaProperty);

        assertEquals(readPreference, mongoClient.getReadPreference());
    }

    @Before
    public void setUp() throws Exception {
        properties = new Properties();
        properties.load(DBAdaptorConnectorTest.class.getResourceAsStream("/eva.properties"));
    }
}
