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


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.eva.lib.utils.QueryOptions;
import uk.ac.ebi.eva.lib.utils.QueryOptionsConstants;
import uk.ac.ebi.eva.lib.utils.QueryResult;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.CHICKEN;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.CHIMPANZEE;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.HUMAN;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.MOUSE;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Sql({"classpath:dgva-schema.sql", "classpath:dgva-data.sql"})
public class ArchiveDgvaDBAdaptorTest {

    @Autowired
    private ArchiveDgvaDBAdaptor archiveDgvaDBAdaptor;

    @Test
    public void countStudies() {
        QueryResult<Long> queryResult = archiveDgvaDBAdaptor.countStudies();

        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(205, queryResult.first().longValue());
    }

    @Test
    public void countStudiesPerSpeciesFilteringBySpecies() {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerSpecies(new QueryOptions(QueryOptionsConstants.SPECIES, HUMAN));

        assertEquals(7, queryResult.getNumTotalResults());
        Map.Entry<String, Long> result = queryResult.first();
        assertEquals(HUMAN, result.getKey());
        assertEquals(155, result.getValue().longValue());
    }

    @Test
    public void countStudiesPerSpeciesFilteringBySpeciesAndType() {
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
    public void countStudiesPerSpeciesUnfiltered() {
        QueryResult<Map.Entry<String, Long>> queryResult =
                archiveDgvaDBAdaptor.countStudiesPerSpecies(new QueryOptions());

        assertEquals(23, queryResult.getNumTotalResults());
        List<Map.Entry<String, Long>> results = queryResult.getResult();
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
    public void countStudiesPerSpeciesFilteringByNonExistingSpecies() {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerSpecies(new QueryOptions(QueryOptionsConstants.SPECIES, "notExistingSpecies"));

        assertEquals(0, queryResult.getNumTotalResults());
    }


    @Test
    public void countStudiesPerTypeFilteringByType() {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerType(new QueryOptions(QueryOptionsConstants.TYPE, DgvaStudyTestData.CONTROL_SET));

        assertEquals(1, queryResult.getNumTotalResults());
        Map.Entry<String, Long> result = queryResult.first();
        assertEquals(DgvaStudyTestData.CONTROL_SET, result.getKey());
        assertEquals(117, result.getValue().longValue());
    }

    @Test
    public void countStudiesPerSpeciesFilteringByNonExistingType() {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerSpecies(new QueryOptions(QueryOptionsConstants.TYPE, "notExistingType"));

        assertEquals(0, queryResult.getNumTotalResults());
    }

    @Test
    public void countStudiesPerTypeUnfiltered() {
        QueryResult<Map.Entry<String, Long>> queryResult = archiveDgvaDBAdaptor
                .countStudiesPerType(new QueryOptions());

        assertEquals(6, queryResult.getNumTotalResults());
        List<Map.Entry<String, Long>> results = queryResult.getResult();
        long controlSetStudiesCount = results.stream().filter(e -> e.getKey().equals(DgvaStudyTestData.CONTROL_SET))
                .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        long collectionStudiesCount = results.stream().filter(e -> e.getKey().equals(DgvaStudyTestData.COLLECTION))
                .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        assertEquals(117, controlSetStudiesCount);
        assertEquals(26, collectionStudiesCount);
    }

    @Test
    public void countFiles() {
        assertThrows(UnsupportedOperationException.class, () -> archiveDgvaDBAdaptor.countFiles());
    }

    @Test
    public void countSpecies() {
        assertThrows(UnsupportedOperationException.class, () -> archiveDgvaDBAdaptor.countSpecies());
    }

    @Test
    public void getBrowsableSpecies() {
        assertThrows(UnsupportedOperationException.class, () -> archiveDgvaDBAdaptor.getBrowsableSpecies());
    }
}