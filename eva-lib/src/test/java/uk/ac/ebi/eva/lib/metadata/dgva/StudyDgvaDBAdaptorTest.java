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
package uk.ac.ebi.eva.lib.metadata.dgva;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.utils.QueryOptions;
import uk.ac.ebi.eva.lib.utils.QueryResult;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class StudyDgvaDBAdaptorTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudyDgvaDBAdaptor studyDgvaDBAdaptor;

    @Before
    public void setUp() throws Exception {
        DgvaStudyTestData.persistTestData(entityManager);
    }

    @Test
    public void getAllStudiesUnfiltered() throws Exception {
        QueryResult<VariantStudy> queryResult = studyDgvaDBAdaptor.getAllStudies(new QueryOptions());

        assertEquals(3, queryResult.getNumTotalResults());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void listStudies() throws Exception {
        studyDgvaDBAdaptor.listStudies();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void findStudyNameOrStudyId() throws Exception {
        studyDgvaDBAdaptor.findStudyNameOrStudyId("Study", new QueryOptions());
    }

    @Test
    public void getStudyById() throws Exception {
        QueryResult<VariantStudy> queryResult = studyDgvaDBAdaptor.getStudyById(DgvaStudyTestData.STUDY_1_ID, new QueryOptions());

        assertEquals(1, queryResult.getNumTotalResults());
        VariantStudy variantStudy = queryResult.first();
        assertEquals(DgvaStudyTestData.STUDY_1_ID, variantStudy.getId());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void close() throws Exception {
        studyDgvaDBAdaptor.close();
    }

}