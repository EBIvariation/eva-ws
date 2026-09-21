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
package uk.ac.ebi.eva.lib.metadata.eva;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.utils.QueryOptions;
import uk.ac.ebi.eva.lib.utils.QueryOptionsConstants;
import uk.ac.ebi.eva.lib.utils.QueryResult;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.HUMAN;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class StudyEvaproDBAdaptorTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudyEvaproDBAdaptor studyEvaproDBAdaptor;

    @BeforeEach
    public void setUp() throws Exception {
        EvaStudyBrowserTestData.persistTestData(entityManager);
    }

    @AfterEach
    public void tearDown() {
        entityManager.clear();
    }

    @Test
    public void getAllStudies() {
        QueryResult<VariantStudy> queryResult = studyEvaproDBAdaptor.getAllStudies(new QueryOptions());

        assertEquals(5, queryResult.getNumTotalResults());
    }

    @Test
    public void getAllStudiesForOneSpecies() {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put(QueryOptionsConstants.SPECIES, HUMAN);
        QueryResult<VariantStudy> queryResult = studyEvaproDBAdaptor.getAllStudies(queryOptions);

        checkReturnedStudies(queryResult, 3,
                study -> study.getSpeciesCommonName().equals(HUMAN));
    }

    @Test
    public void getAllStudiesForOneType() {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put(QueryOptionsConstants.TYPE, EvaStudyBrowserTestData.EXOME_SEQUENCING);
        QueryResult<VariantStudy> queryResult = studyEvaproDBAdaptor.getAllStudies(queryOptions);

        checkReturnedStudies(queryResult, 2,
                study -> study.getExperimentType().equals(EvaStudyBrowserTestData.EXOME_SEQUENCING));
    }

    @Test
    public void getAllStudiesForAnSpeciesAndType() {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put(QueryOptionsConstants.SPECIES, HUMAN);
        queryOptions.put(QueryOptionsConstants.TYPE, EvaStudyBrowserTestData.EXOME_SEQUENCING);
        QueryResult<VariantStudy> queryResult = studyEvaproDBAdaptor.getAllStudies(queryOptions);

        checkReturnedStudies(queryResult, 1,
                study -> study.getSpeciesCommonName().equals(HUMAN) && study
                        .getExperimentType()
                        .equals(EvaStudyBrowserTestData.EXOME_SEQUENCING));
    }

    @Test
    public void listStudies() {
        assertThrows(UnsupportedOperationException.class, () -> studyEvaproDBAdaptor.listStudies());
    }

    @Test
    public void findStudyNameOrStudyId() {
        assertThrows(UnsupportedOperationException.class, () -> studyEvaproDBAdaptor.findStudyNameOrStudyId("any study", new QueryOptions()));
    }

    @Test
    public void getStudyById() {
        QueryResult<VariantStudy> queryResult = studyEvaproDBAdaptor
                .getStudyById(EvaStudyBrowserTestData.PROJECT_ID_1, new QueryOptions());

        checkReturnedStudies(queryResult, 1, study -> study.getId().equals(EvaStudyBrowserTestData.PROJECT_ID_1));
    }

    @Test
    public void close() {
        assertThrows(UnsupportedOperationException.class, () -> studyEvaproDBAdaptor.close());
    }

    private void checkReturnedStudies(QueryResult<VariantStudy> queryResult, int expectedNumberOfResults,
                                      Predicate<VariantStudy> predicate) {
        assertEquals(expectedNumberOfResults, queryResult.getNumTotalResults());
        assertTrue(queryResult.getResult().stream().allMatch(predicate));
    }
}