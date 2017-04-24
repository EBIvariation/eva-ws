package uk.ac.ebi.eva.lib.repository;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.ebi.eva.commons.models.metadata.AnnotationMetadata;
import uk.ac.ebi.eva.lib.configuration.MongoRepositoryTestConfiguration;

import java.util.List;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoRepositoryTestConfiguration.class})
@UsingDataSet(locations = {"/test-data/annotation_metadata.json"})
public class AnnotationMetadataRepositoryTest {

    private static final String TEST_DB = "test-db";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AnnotationMetadataRepository repository;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb(TEST_DB);

    @Test
    public void testFindAllByOrderByCacheVersionDescVepVersionDescSize() throws Exception {
        List<AnnotationMetadata> annotationMetadataList = repository.findAllByOrderByCacheVersionDescVepVersionDesc();
        assertEquals(4, annotationMetadataList.size());
    }

    @Test
    public void testFindAllByOrderByCacheVersionDescVepVersionDescCacheVersionOrder() throws Exception {
        List<AnnotationMetadata> annotationMetadataList = repository.findAllByOrderByCacheVersionDescVepVersionDesc();
        AnnotationMetadata prevAnnotationMetadata = annotationMetadataList.get(0);
        for (AnnotationMetadata curAnnotationMetadata :
                annotationMetadataList.subList(1, annotationMetadataList.size())) {
            assertTrue(Integer.parseInt(curAnnotationMetadata.getCacheVersion())
                               <= Integer.parseInt(prevAnnotationMetadata.getCacheVersion()));
        }
    }

    @Test
    public void testFindAllByOrderByCacheVersionDescVepVersionDescVepVersionOrder() throws Exception {
        List<AnnotationMetadata> annotationMetadataList = repository.findAllByOrderByCacheVersionDescVepVersionDesc();
        AnnotationMetadata prevAnnotationMetadata = annotationMetadataList.get(0);
        for (AnnotationMetadata curAnnotationMetadata :
                annotationMetadataList.subList(1, annotationMetadataList.size())) {
            if (curAnnotationMetadata.getCacheVersion().equals(prevAnnotationMetadata.getCacheVersion())) {
                assertTrue(Integer.parseInt(curAnnotationMetadata.getCacheVersion())
                                   <= Integer.parseInt(prevAnnotationMetadata.getCacheVersion()));
            }
        }
    }

}
