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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoRepositoryTestConfiguration.class})
@UsingDataSet(locations = {"/test-data/files.json"})
public class VariantSourceEntityRepositoryTest {

    protected static Logger logger = LoggerFactory.getLogger(VariantSourceEntityRepositoryTest.class);

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
        assertEquals(3, variantSourceEntityList.size());
    }

    @Test
    public void testFindByStudyIdOrStudyName() {
        List<VariantSourceEntity> variantSourceEntityList = repository.findByStudyIdOrStudyName("firstStudyId", "firstStudyId");
        assertEquals(1, variantSourceEntityList.size());
        variantSourceEntityList = repository.findByStudyIdOrStudyName("secondStudyId", "secondStudyId");
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
        studyIds.add("secondStudyId");

        Pageable pageable = new PageRequest(0, 1);
        List<VariantSourceEntity> variantSourceEntityList = repository.findByStudyIdIn(studyIds, pageable);
        assertEquals(1, variantSourceEntityList.size());

        pageable = new PageRequest(0, 2);
        variantSourceEntityList = repository.findByStudyIdIn(studyIds, pageable);
        assertEquals(2, variantSourceEntityList.size());

        studyIds.add("firstStudyId");

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
        studyIds.add("secondStudyId");

        int count = repository.countByStudyIdIn(studyIds);
        assertEquals(2, count);

        studyIds.add("firstStudyId");

        count = repository.countByStudyIdIn(studyIds);
        assertEquals(3, count);
    }


}
