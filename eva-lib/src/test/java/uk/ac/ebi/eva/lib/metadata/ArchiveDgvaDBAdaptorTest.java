/*
 * Copyright 2014-2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.lib.metadata;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.lib.utils.QueryOptionsConstants;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ArchiveDgvaDBAdaptorTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ArchiveDgvaDBAdaptor archiveDgvaDBAdaptor;

    @Before
    public void setUp() throws Exception {
        DgvaStudyTestData.persistTestData(entityManager);
    }

    @Test
    public void countStudies() throws Exception {
        QueryResult<Long> queryResult = archiveDgvaDBAdaptor.countStudies();

        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(3, queryResult.getResult().get(0).longValue());
    }

    @Test
    public void countStudiesPerSpeciesFilteringBySpecies() throws Exception {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerSpecies(new QueryOptions(QueryOptionsConstants.SPECIES, DgvaStudyTestData.HUMAN));

        assertEquals(1, queryResult.getNumTotalResults());
        Map.Entry<String, Long> result = queryResult.getResult().get(0);
        assertEquals(DgvaStudyTestData.HUMAN, result.getKey());
        assertEquals(2, result.getValue().longValue());
    }

    @Test
    public void countStudiesPerSpeciesFilteringBySpeciesAndType() throws Exception {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put(QueryOptionsConstants.SPECIES, DgvaStudyTestData.HUMAN);
        queryOptions.put(QueryOptionsConstants.TYPE, DgvaStudyTestData.CONTROL_SET);
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor.countStudiesPerSpecies(queryOptions);

        assertEquals(1, queryResult.getNumTotalResults());
        Map.Entry<String, Long> result = queryResult.getResult().get(0);
        assertEquals(DgvaStudyTestData.HUMAN, result.getKey());
        assertEquals(1, result.getValue().longValue());
    }

    @Test
    public void countStudiesPerSpeciesUnfiltered() throws Exception {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerSpecies(new QueryOptions());

        assertEquals(2, queryResult.getNumTotalResults());
        List<Map.Entry<String, Long>> results =  queryResult.getResult();
        long humanStudiesCount = results.stream().filter(e -> e.getKey().equals(DgvaStudyTestData.HUMAN))
                                        .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        long mouseStudiesCount = results.stream().filter(e -> e.getKey().equals(DgvaStudyTestData.MOUSE))
                                      .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        assertEquals(2, humanStudiesCount);
        assertEquals(1, mouseStudiesCount);
    }

    @Test
    public void countStudiesPerTypeFilteringByType() throws Exception {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerType(new QueryOptions(QueryOptionsConstants.TYPE, DgvaStudyTestData.CONTROL_SET));

        assertEquals(1, queryResult.getNumTotalResults());
        Map.Entry<String, Long> result = queryResult.getResult().get(0);
        assertEquals(DgvaStudyTestData.CONTROL_SET, result.getKey());
        assertEquals(2, result.getValue().longValue());
    }

    @Test
    public void countStudiesPerTypeUnfiltered() throws Exception {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerType(new QueryOptions());

        assertEquals(2, queryResult.getNumTotalResults());
        List<Map.Entry<String, Long>> results =  queryResult.getResult();
        long controlSetStudiesCount = results.stream().filter(e -> e.getKey().equals(DgvaStudyTestData.CONTROL_SET))
                                        .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        long collectionStudiesCount = results.stream().filter(e -> e.getKey().equals(DgvaStudyTestData.COLLECTION))
                                        .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        assertEquals(2, controlSetStudiesCount);
        assertEquals(1, collectionStudiesCount);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void countFiles() throws Exception {
        archiveDgvaDBAdaptor.countFiles();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void countSpecies() throws Exception {
        archiveDgvaDBAdaptor.countSpecies();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSpecies() throws Exception {
        archiveDgvaDBAdaptor.getSpecies(DgvaStudyTestData.HUMAN, true);
    }
}