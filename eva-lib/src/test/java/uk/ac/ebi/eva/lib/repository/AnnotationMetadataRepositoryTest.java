/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            assertTrue(curAnnotationMetadata.getCacheVersion().compareTo(prevAnnotationMetadata.getCacheVersion())
                               <= 0);
        }
    }

    @Test
    public void testFindAllByOrderByCacheVersionDescVepVersionDescVepVersionOrder() throws Exception {
        List<AnnotationMetadata> annotationMetadataList = repository.findAllByOrderByCacheVersionDescVepVersionDesc();
        AnnotationMetadata prevAnnotationMetadata = annotationMetadataList.get(0);
        for (AnnotationMetadata curAnnotationMetadata :
                annotationMetadataList.subList(1, annotationMetadataList.size())) {
            if (curAnnotationMetadata.getCacheVersion().equals(prevAnnotationMetadata.getCacheVersion())) {
                assertTrue(curAnnotationMetadata.getCacheVersion().compareTo(prevAnnotationMetadata.getCacheVersion())
                                   <= 0);
            }
        }
    }

}
