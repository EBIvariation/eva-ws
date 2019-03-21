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
package uk.ac.ebi.eva.lib.metadata.dgva;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.lib.utils.QueryOptions;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.lib.utils.QueryOptionsConstants;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.CHICKEN;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.CHIMPANZEE;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.HUMAN;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.MOUSE;

@RunWith(SpringRunner.class)
@DataJpaTest
@Sql({"classpath:dgva-schema.sql", "classpath:dgva-data.sql"})
public class ArchiveDgvaDBAdaptorTest {

    @Autowired
    private ArchiveDgvaDBAdaptor archiveDgvaDBAdaptor;

    @Test
    public void countStudies() throws Exception {
        QueryResult<Long> queryResult = archiveDgvaDBAdaptor.countStudies();

        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(205, queryResult.first().longValue());
    }

    @Test
    public void countStudiesPerSpeciesFilteringBySpecies() throws Exception {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerSpecies(new QueryOptions(QueryOptionsConstants.SPECIES, HUMAN));

        assertEquals(7, queryResult.getNumTotalResults());
        Map.Entry<String, Long> result = queryResult.first();
        assertEquals(HUMAN, result.getKey());
        assertEquals(155, result.getValue().longValue());
    }

    @Test
    public void countStudiesPerSpeciesFilteringBySpeciesAndType() throws Exception {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put(QueryOptionsConstants.SPECIES, HUMAN);
        queryOptions.put(QueryOptionsConstants.TYPE, DgvaStudyTestData.CONTROL_SET);
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor.countStudiesPerSpecies(queryOptions);

        assertEquals(7, queryResult.getNumTotalResults());
        Map.Entry<String, Long> result = queryResult.first();
        assertEquals(HUMAN, result.getKey());
        assertEquals(84, result.getValue().longValue());
    }

    @Test
    public void countStudiesPerSpeciesUnfiltered() throws Exception {
        QueryResult<Map.Entry<String, Long>> queryResult =
                archiveDgvaDBAdaptor.countStudiesPerSpecies(new QueryOptions());

        assertEquals(23, queryResult.getNumTotalResults());
        List<Map.Entry<String, Long>> results =  queryResult.getResult();
        long chickenStudiesCount = results.stream().filter(e -> e.getKey().equals(CHICKEN))
                                        .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        long chimpanzeeStudiesCount = results.stream().filter(e -> e.getKey().equals(CHIMPANZEE))
                                          .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        long humanStudiesCount = results.stream().filter(e -> e.getKey().equals(HUMAN))
                                        .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        long mouseStudiesCount = results.stream().filter(e -> e.getKey().equals(MOUSE))
                                      .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        assertEquals(3, chickenStudiesCount);
        assertEquals(4, chimpanzeeStudiesCount);
        assertEquals(155, humanStudiesCount);
        assertEquals(13, mouseStudiesCount);
    }

    @Test
    public void countStudiesPerSpeciesFilteringByNonExistingSpecies() throws Exception {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerSpecies(new QueryOptions(QueryOptionsConstants.SPECIES, "notExistingSpecies"));

        assertEquals(0, queryResult.getNumTotalResults());
    }


    @Test
    public void countStudiesPerTypeFilteringByType() throws Exception {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerType(new QueryOptions(QueryOptionsConstants.TYPE, DgvaStudyTestData.CONTROL_SET));

        assertEquals(1, queryResult.getNumTotalResults());
        Map.Entry<String, Long> result = queryResult.first();
        assertEquals(DgvaStudyTestData.CONTROL_SET, result.getKey());
        assertEquals(117, result.getValue().longValue());
    }

    @Test
    public void countStudiesPerSpeciesFilteringByNonExistingType() throws Exception {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerSpecies(new QueryOptions(QueryOptionsConstants.TYPE, "notExistingType"));

        assertEquals(0, queryResult.getNumTotalResults());
    }

    @Test
    public void countStudiesPerTypeUnfiltered() throws Exception {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerType(new QueryOptions());

        assertEquals(6, queryResult.getNumTotalResults());
        List<Map.Entry<String, Long>> results =  queryResult.getResult();
        long controlSetStudiesCount = results.stream().filter(e -> e.getKey().equals(DgvaStudyTestData.CONTROL_SET))
                                        .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        long collectionStudiesCount = results.stream().filter(e -> e.getKey().equals(DgvaStudyTestData.COLLECTION))
                                        .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        assertEquals(117, controlSetStudiesCount);
        assertEquals(26, collectionStudiesCount);
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
    public void getBrowsableSpecies() throws Exception {
        archiveDgvaDBAdaptor.getBrowsableSpecies();
    }
}