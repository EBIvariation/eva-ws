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
import org.opencb.biodata.models.variant.stats.VariantGlobalStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.ebi.eva.commons.models.data.VariantSourceEntity;
import uk.ac.ebi.eva.lib.configuration.MongoRepositoryTestConfiguration;

import java.util.ArrayList;
import java.util.List;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoRepositoryTestConfiguration.class})
@UsingDataSet(locations = {"/test-data/files.json"})
public class VariantSourceEntityRepositoryTest {

    protected static Logger logger = LoggerFactory.getLogger(VariantSourceEntityRepositoryTest.class);

    private static final String FIRST_STUDY_ID = "firstStudyId";
    private static final String SECOND_STUDY_ID = "secondStudyId";

    private static final String FIRST_FILE_ID = "firstFileId";
    private static final String SECOND_FILE_ID = "secondFileId";

    private static final String TEST_DB = "test-db";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private VariantSourceEntityRepository repository;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb(TEST_DB);

    @Test
    public void testFindAll() {
        List<VariantSourceEntity> variantSourceEntityList = repository.findAll();
        assertEquals(24, variantSourceEntityList.size());
    }

    @Test
    public void testFindByStudyIdOrStudyName() {
        List<VariantSourceEntity> variantSourceEntityList = repository.findByStudyIdOrStudyName(FIRST_STUDY_ID, FIRST_STUDY_ID);
        assertEquals(1, variantSourceEntityList.size());
        variantSourceEntityList = repository.findByStudyIdOrStudyName(SECOND_STUDY_ID, SECOND_STUDY_ID);
        assertEquals(2, variantSourceEntityList.size());
    }

    @Test
    public void testFindByStudyIdOrStudyNameTestNonExistent() {
        List<VariantSourceEntity> variantSourceEntityList = repository.findByStudyIdOrStudyName("notARealId", "notARealId");
        assertEquals(0, variantSourceEntityList.size());
    }

    @Test
    public void testFindByStudyId() {
        List<String> studyIds = new ArrayList<>();
        studyIds.add(SECOND_STUDY_ID);

        Pageable pageable = new PageRequest(0, 1);
        List<VariantSourceEntity> variantSourceEntityList = repository.findByStudyIdIn(studyIds, pageable);
        assertEquals(1, variantSourceEntityList.size());

        pageable = new PageRequest(0, 2);
        variantSourceEntityList = repository.findByStudyIdIn(studyIds, pageable);
        assertEquals(2, variantSourceEntityList.size());

        studyIds.add(FIRST_STUDY_ID);

        pageable = new PageRequest(1, 2);
        variantSourceEntityList = repository.findByStudyIdIn(studyIds, pageable);
        assertEquals(1, variantSourceEntityList.size());

        pageable = new PageRequest(2, 2);
        variantSourceEntityList = repository.findByStudyIdIn(studyIds, pageable);
        assertEquals(0, variantSourceEntityList.size());
    }

    @Test
    public void testCountByStudyIdIn() {
        List<String> studyIds = new ArrayList<>();
        studyIds.add(SECOND_STUDY_ID);

        long count = repository.countByStudyIdIn(studyIds);
        assertEquals(2, count);

        studyIds.add(FIRST_STUDY_ID);

        count = repository.countByStudyIdIn(studyIds);
        assertEquals(3, count);
    }

    @Test
    public void testFindByFileIdIn() {
        List<String> fileIds = new ArrayList<>();
        fileIds.add(FIRST_FILE_ID);

        Pageable pageable = new PageRequest(0, 100);
        List<VariantSourceEntity> variantSourceEntityList = repository.findByFileIdIn(fileIds, pageable);
        assertEquals(1, variantSourceEntityList.size());

        for (VariantSourceEntity variantSourceEntity : variantSourceEntityList) {
            assertFalse(variantSourceEntity.getSamplesPosition().isEmpty());
            assertEquals(FIRST_FILE_ID, variantSourceEntity.getFileId());
        }
    }

    @Test
    public void testStatsNotZero() {
        List<String> fileIds = new ArrayList<>();
        fileIds.add(FIRST_FILE_ID);

        Pageable pageable = new PageRequest(0, 100);
        List<VariantSourceEntity> variantSourceEntityList = repository.findByFileIdIn(fileIds, pageable);
        assertEquals(1, variantSourceEntityList.size());

        VariantSourceEntity variantSourceEntity = variantSourceEntityList.get(0);
        VariantGlobalStats variantGlobalStats = variantSourceEntity.getStats();

        assertNotEquals(0, variantGlobalStats.getSamplesCount());
        assertNotEquals(0, variantGlobalStats.getVariantsCount());
        assertNotEquals(0, variantGlobalStats.getSnpsCount());
        assertNotEquals(0, variantGlobalStats.getIndelsCount());
        assertNotEquals(0, variantGlobalStats.getPassCount());
        assertNotEquals(0, variantGlobalStats.getTransitionsCount());
        assertNotEquals(0, variantGlobalStats.getTransversionsCount());
        assertNotEquals(0, variantGlobalStats.getMeanQuality());
    }

    @Test
    public void testCountByFileIdIn() {
        List<String> fileIds = new ArrayList<>();
        fileIds.add(SECOND_FILE_ID);

        long count = repository.countByFileIdIn(fileIds);
        assertEquals(1, count);

        fileIds.add(FIRST_FILE_ID);

        count = repository.countByFileIdIn(fileIds);
        assertEquals(2, count);
    }


}
