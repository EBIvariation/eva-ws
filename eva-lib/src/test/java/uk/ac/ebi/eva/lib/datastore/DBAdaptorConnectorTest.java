package uk.ac.ebi.eva.lib.datastore;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.mongodb.MongoDbConfigurationBuilder;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoDatabase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.lib.MongoConfiguration;
import uk.ac.ebi.eva.lib.MultiMongoFactoryConfiguration;
import uk.ac.ebi.eva.lib.configuration.MongoRepositoryTestConfiguration;
import uk.ac.ebi.eva.lib.configuration.SpringDataMongoDbProperties;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.lib.test.rule.FixSpringMongoDbRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@UsingDataSet(locations = {
        "/test-data/annotation_metadata.json",
        "/test-data/files.json",
        "/test-data/variants.json"
})
@ContextConfiguration(classes = { MongoConfiguration.class, MultiMongoFactoryConfiguration.class,
        MongoRepositoryTestConfiguration.class})
@SpringBootTest
@EnableConfigurationProperties
public class DBAdaptorConnectorTest {

    private static final String TEST_DB = "test-db";

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = new FixSpringMongoDbRule(
            MongoDbConfigurationBuilder.mongoDb().databaseName(TEST_DB).build());

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
        String dbName = "test-db";
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(dbName);
        MongoDatabase db = factory.getDb();
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
