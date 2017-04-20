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
@SpringBootTest(classes = {EvaPropertiesTestConfiguration.class})
@EnableConfigurationProperties
public class EvaPropertiesTest {

    @Autowired
    private EvaProperties evaProperties;

    @Value("${eva.mongo.host}")
    private String expectedHostProp;

    @Test
    public void testEvaPropertyAutowiring() {
        assertNotNull(evaProperties);
        assertNotNull(evaProperties.getMongo());
        assertEquals(expectedHostProp, evaProperties.getMongo().getHost());
    }

}
