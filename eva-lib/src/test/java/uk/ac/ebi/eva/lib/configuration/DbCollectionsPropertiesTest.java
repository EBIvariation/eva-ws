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
@SpringBootTest(classes = {DbCollectionsPropertiesTestConfiguration.class})
@EnableConfigurationProperties
public class DbCollectionsPropertiesTest {

    @Autowired
    private DbCollectionsProperties dbCollectionsProperties;

    @Value("${db.collections.files.name}")
    private String expectedFiles;

    @Test
    public void testEvaPropertyAutowiring() {
        assertNotNull(dbCollectionsProperties);
        assertNotNull(dbCollectionsProperties.getVariants());
        assertEquals(expectedFiles, dbCollectionsProperties.getFiles().getName());
    }

}
