package uk.ac.ebi.eva.lib.datastore;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.ebi.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.datastore.MultiMongoDbFactory;
import uk.ac.ebi.eva.server.ApplicationConfiguration;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author Jose Miguel Mut Lopez &lt;jmmut@ebi.ac.uk&gt;
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfiguration.class })
public class DBAdaptorConnectorTest {

    @Autowired
    private MongoDbFactory factory;

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
        Properties properties = new Properties();
        properties.load(DBAdaptorConnectorTest.class.getResourceAsStream("/eva.properties"));
        MongoClient mongoClient = DBAdaptorConnector.getMongoClient(properties);

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
        Properties properties = new Properties();
        properties.load(DBAdaptorConnectorTest.class.getResourceAsStream("/eva.properties"));
        properties.put("eva.mongo.read-preference", "nearest");
        MongoClient mongoClient = DBAdaptorConnector.getMongoClient(properties);

        assertEquals(ReadPreference.nearest(), mongoClient.getReadPreference());
    }

}
