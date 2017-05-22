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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.ebi.eva.lib.configuration.MongoRepositoryTestConfiguration;
import uk.ac.ebi.eva.lib.repository.projections.VariantStudySummary;

import java.util.Iterator;
import java.util.List;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoRepositoryTestConfiguration.class})
@UsingDataSet(locations = {"/test-data/files.json"})
public class VariantStudySummaryRepositoryTest {

    protected static Logger logger = LoggerFactory.getLogger(VariantStudySummaryRepositoryTest.class);

    private static final String FIRST_STUDY_NAME = "firstStudyName";
    private static final String SECOND_STUDY_NAME = "secondStudyName";

    private static final String FIRST_STUDY_ID = "firstStudyId";
    private static final String SECOND_STUDY_ID = "secondStudyId";

    private static final String TEST_DB = "test-db";

    private static final int EXPECTED_UNIQUE_STUDIES_COUNT = 18;

    private static final int EXPECTED_FILE_COUNT_FROM_FIRST_STUDY_ID = 1;
    private static final int EXPECTED_FILE_COUNT_FROM_SECOND_STUDY_ID = 2;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private VariantStudySummaryRepository repository;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb(TEST_DB);

    @Test
    public void testFindsByNameOrIdProvidingName() {
        assertFindBySecondNameOrId(SECOND_STUDY_NAME);
    }

    @Test
    public void testFindsByNameOrIdProvidingId() {
        assertFindBySecondNameOrId(SECOND_STUDY_ID);
    }

    private void assertFindBySecondNameOrId(String studyNameOrId) {
        VariantStudySummary study = repository.findByStudyNameOrStudyId(studyNameOrId);
        assertNotNull(study);
        assertEquals(SECOND_STUDY_ID, study.getStudyId());
        assertEquals(SECOND_STUDY_NAME, study.getStudyName());
        assertCorrectCount(EXPECTED_FILE_COUNT_FROM_SECOND_STUDY_ID, study);
    }

    @Test
    public void testDoesntFindNonPresentStudies() throws Exception {
        VariantStudySummary study = repository.findByStudyNameOrStudyId("wrongStudyId");
        assertNull(study);
    }

    @Test
    public void testListStudies() {
        List<VariantStudySummary> uniqueStudies = repository.findBy();
        assertEquals(EXPECTED_UNIQUE_STUDIES_COUNT, uniqueStudies.size());
    }

    private void assertCorrectCount(int expectedFileCount, VariantStudySummary study) {
        int buggedFongoCount = 0;
        if (study.getFilesCount() == buggedFongoCount) {
            logger.warn("Although the expected files count is different from the actual one ({} != {}) " +
                    "this is a known limitation of Fongo, in a real mongo it works, " +
                    "see https://github.com/fakemongo/fongo/issues/258", expectedFileCount, study.getFilesCount());
        } else {
            assertEquals(expectedFileCount, study.getFilesCount());
        }

    }
}


