package uk.ac.ebi.eva.lib.configuration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringDataMongoDbPropertiesTestConfiguration.class})
@EnableConfigurationProperties
public class SpringDataMongoDbPropertiesTest {

    @Autowired
    private SpringDataMongoDbProperties properties;

    @Value("${spring.data.mongodb.host}")
    private String expectedHost;

    @Value("${spring.data.mongodb.username}")
    private String expectedUsername;

    @Test
    public void testEvaPropertyAutowiring() {
        assertNotNull(properties);
        assertNotNull(properties.getHost());
        assertEquals(expectedHost, properties.getHost());
        assertEquals(expectedUsername, properties.getUsername());
    }

}
