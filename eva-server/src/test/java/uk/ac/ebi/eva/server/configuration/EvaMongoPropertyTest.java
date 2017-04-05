package uk.ac.ebi.eva.server.configuration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.lib.config.EvaMongoProperty;
import uk.ac.ebi.eva.server.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class EvaMongoPropertyTest {

//    @Autowired
//    private EvaMongoProperty evaMongoProperty;

    @Value("${eva.mongo.host}")
    private String expectedHostProp;

    @Test
    public void testLoadingOfProperties() {
        System.out.println("expectedHostProp: " + expectedHostProp);
//        assertEquals(expectedHostProp, evaMongoProperty.getHost());
    }

}