package uk.ac.ebi.eva.server.configuration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.lib.config.EvaProperty;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EvaMongoPropertyTest {

    @Autowired
    private EvaProperty evaProperty;

    @Value("${eva.mongo.host}")
    private String expectedHostProp;

    @Test
    public void testLoadingOfProperties() {
        System.out.println("expectedHostProp: " + expectedHostProp);
        assertEquals(expectedHostProp, evaProperty.getMongo().getHost());
    }

}
