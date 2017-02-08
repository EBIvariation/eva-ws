/*
 * Copyright 2016-2017 EMBL - European Bioinformatics Institute
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.ebi.eva.lib.repository.projections.StudyName;
import uk.ac.ebi.eva.lib.configuration.MongoRepositoryTestConfiguration;
import uk.ac.ebi.eva.commons.models.data.VariantSourceEntity;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoRepositoryTestConfiguration.class})
@UsingDataSet(locations = {"/test-data/files.json"})
public class VariantSourceEntityRepositoryTest {

    private static final String STUDY_NAME = "studyName";
    private static final String OTHER_STUDY_NAME = "anotherStudyName";

    private static final String STUDY_ID = "studyId";
    private static final String OTHER_STUDY_ID = "anotherStudyId";

    private static final String TEST_DB = "test-db";

    private static final int EXPECTED_STUDIES_COUNT = 2;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private VariantSourceEntityRepository repository;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb(TEST_DB);

    @Value(value = "#{mongoCollectionsFiles}")
    private String mongoCollectionName;

    @Value("${eva.mongo.collections.files}")
    private String directMongoTestCollectionName;

    @Test
    public void findsByNameOrIdProvidingName() {
        String studyNameOrId = STUDY_NAME;
        VariantSourceEntity study = repository.findByStudyNameOrStudyId(studyNameOrId, studyNameOrId);
        assertNotNull(study);
        assertEquals(STUDY_ID, study.getStudyId());
    }

    @Test
    public void findsByNameOrIdProvidingId() {
        String studyNameOrId = STUDY_ID;
        VariantSourceEntity study = repository.findByStudyNameOrStudyId(studyNameOrId, studyNameOrId);
        assertNotNull(study);
        assertEquals(STUDY_ID, study.getStudyId());
    }

    @Test
    public void findsById() {
        VariantSourceEntity study = repository.findByStudyId(STUDY_ID);
        assertNotNull(study);
        assertEquals(STUDY_ID, study.getStudyId());
    }

    @Test
    public void listStudies() {
        List<StudyName> nonUniqueStudies = repository.findBy();
        assertNotNull(nonUniqueStudies);

        Set<StudyName> uniqueStudies = new TreeSet<>(nonUniqueStudies);

        Iterator<StudyName> studiesIterator = uniqueStudies.iterator();
        StudyName next = studiesIterator.next();
        assertEquals(OTHER_STUDY_ID, next.getStudyId());
        assertEquals(OTHER_STUDY_NAME, next.getStudyName());
        next = studiesIterator.next();
        assertEquals(STUDY_ID, next.getStudyId());
        assertEquals(STUDY_NAME, next.getStudyName());
    }
}


