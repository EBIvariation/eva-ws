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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.lib.entities.Project;
import uk.ac.ebi.eva.lib.entities.Taxonomy;
import uk.ac.ebi.eva.lib.metadata.FileTestData;
import uk.ac.ebi.eva.lib.models.Assembly;
import uk.ac.ebi.eva.lib.utils.QueryOptions;
import uk.ac.ebi.eva.lib.utils.QueryOptionsConstants;
import uk.ac.ebi.eva.lib.utils.QueryResult;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.BOS_TAURUS;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.CATTLE;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.HOMO_SAPIENS;
import static uk.ac.ebi.eva.lib.metadata.MetadataTestData.HUMAN;

@RunWith(SpringRunner.class)
@DataJpaTest
@Sql({"classpath:eva-schema.sql", "classpath:eva-data.sql"})
public class ArchiveEvaproDBAdaptorTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ArchiveEvaproDBAdaptor archiveEvaproDBAdaptor;

    @Before
    public void setUp() throws Exception {
        Project prj1 = new Project("PRJEB1", "EBI", "PRJ 1", "Project 1 title", "Project 1 description",
                                   "multi-isolate", "DNA", "genome", "SUBMISSION", "ERP1", "Germline", 1L,
                                   "Variation data project 1", "European Bioinformatics Institute",
                                   "http://www.ebi.ac.uk", 1L, "Control Set");
        Project prj2 = new Project("PRJEB2", "EBI", "PRJ 2", "Project 2 title", "Project 2 description",
                                   "multi-isolate", "DNA", "genome", "SUBMISSION", "ERP2", "Germline", 2L,
                                   "Variation data project 2", "European Bioinformatics Institute",
                                   "http://www.ebi.ac.uk", 2L, "Control Set");

        entityManager.persist(prj1);
        entityManager.persist(prj2);

        Taxonomy humanTaxonomy = new Taxonomy(9606L, HUMAN, HOMO_SAPIENS, "hsapiens", "human");
        Taxonomy cowTaxonomy = new Taxonomy(9913L, CATTLE, BOS_TAURUS, "btaurus", "cow");
        Taxonomy chickenTaxonomy = new Taxonomy(9031L, "Chicken", "Gallus gallus", "ggallus", "chicken");
        Taxonomy sheepTaxonomy = new Taxonomy(9940L, "Sheep", "Ovis aries", "oaries", "sheep");
        Taxonomy goatTaxonomy = new Taxonomy(9925L, "Goat", "Capra hircus", "chircus", "goat");
        Taxonomy pigTaxonomy = new Taxonomy(9823L, "Pig", "Sus scrofa", "sscrofa", "pig");
        Taxonomy zebrafishTaxonomy = new Taxonomy(7955L, "Zebrafish", "Dario rerio", null, "zebrafish");

        entityManager.persist(humanTaxonomy);
        entityManager.persist(cowTaxonomy);
        entityManager.persist(chickenTaxonomy);
        entityManager.persist(sheepTaxonomy);
        entityManager.persist(goatTaxonomy);
        entityManager.persist(pigTaxonomy);
        entityManager.persist(zebrafishTaxonomy);

        EvaStudyBrowserTestData.persistTestData(entityManager);

        FileTestData.persistTestData(entityManager);
    }

    @Test
    public void countStudies() throws Exception {
        QueryResult<Long> countStudiesResult = archiveEvaproDBAdaptor.countStudies();

        assertEquals(1, countStudiesResult.getNumResults());
        assertEquals(2, countStudiesResult.first().longValue());
    }

    @Test
    public void countStudiesPerSpeciesFilteringBySpecies() throws Exception {
        QueryResult<Map.Entry<String, Long>> countStudiesResult = archiveEvaproDBAdaptor
                .countStudiesPerSpecies(new QueryOptions(QueryOptionsConstants.SPECIES, HUMAN));

        assertEquals(1, countStudiesResult.getNumResults());
        Map.Entry<String, Long> result = countStudiesResult.first();
        assertEquals(HUMAN, result.getKey());
        assertEquals(3, result.getValue().longValue());
    }

    @Test
    public void countStudiesPerSpeciesFilteringBySpeciesAndtype() throws Exception {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put(QueryOptionsConstants.SPECIES, HUMAN);
        queryOptions.put(QueryOptionsConstants.TYPE, EvaStudyBrowserTestData.EXOME_SEQUENCING);

        QueryResult<Map.Entry<String, Long>> countStudiesResult = archiveEvaproDBAdaptor
                .countStudiesPerSpecies(queryOptions);

        assertEquals(1, countStudiesResult.getNumResults());
        Map.Entry<String, Long> result = countStudiesResult.first();
        assertEquals(HUMAN, result.getKey());
        assertEquals(1, result.getValue().longValue());
    }

    @Test
    public void countStudiesPerSpeciesFilteringByNotExistingSpecies() throws Exception {
        QueryResult<Map.Entry<String, Long>> countStudiesResult = archiveEvaproDBAdaptor
                .countStudiesPerSpecies(new QueryOptions(QueryOptionsConstants.SPECIES, "NotExistingSpecies"));

        assertEquals(0, countStudiesResult.getNumResults());
    }

    @Test
    public void countStudiesPerSpeciesUnfiltered() throws Exception {
        QueryResult<Map.Entry<String, Long>> countStudiesResult = archiveEvaproDBAdaptor
                .countStudiesPerSpecies(new QueryOptions());

        assertEquals(2, countStudiesResult.getNumResults());
        List<Map.Entry<String, Long>> results =  countStudiesResult.getResult();
        long humanStudiesCount = results.stream().filter(e -> e.getKey().equals(HUMAN))
                                        .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        long cowStudiesCount = results.stream().filter(e -> e.getKey().equals(CATTLE))
                                      .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        assertEquals(3, humanStudiesCount);
        assertEquals(3, cowStudiesCount);
    }

    @Test
    public void countStudiesPerTypeFilteringByType() throws Exception {
        QueryResult<Map.Entry<String, Long>> countStudiesResult = archiveEvaproDBAdaptor
                .countStudiesPerType(
                        new QueryOptions(QueryOptionsConstants.TYPE, EvaStudyBrowserTestData.EXOME_SEQUENCING));
        assertEquals(1, countStudiesResult.getNumResults());
        Map.Entry<String, Long> result = countStudiesResult.first();
        assertEquals(EvaStudyBrowserTestData.EXOME_SEQUENCING, result.getKey());
        assertEquals(2, result.getValue().longValue());
    }

    @Test
    public void countStudiesPerTypeFilteringByNotExistingType() throws Exception {
        QueryResult<Map.Entry<String, Long>> countStudiesResult = archiveEvaproDBAdaptor
                .countStudiesPerType(
                        new QueryOptions(QueryOptionsConstants.TYPE, "NotExistingType"));
        assertEquals(0, countStudiesResult.getNumResults());
        Map.Entry<String, Long> result = countStudiesResult.first();
    }

    @Test
    public void countStudiesPerTypeUnfiltered() throws Exception {
        QueryResult<Map.Entry<String, Long>> countStudiesResult = archiveEvaproDBAdaptor
                .countStudiesPerType(new QueryOptions());

        assertEquals(3, countStudiesResult.getNumResults());
        List<Map.Entry<String, Long>> results =  countStudiesResult.getResult();
        long wgsStudiesCount = results.stream()
                                      .filter(e -> e.getKey().equals(EvaStudyBrowserTestData.WHOLE_GENOME_SEQUENCING))
                                      .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        long rnaSeqStudiesCount = results.stream().filter(e -> e.getKey().equals(EvaStudyBrowserTestData.RNA_SEQ))
                                      .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        long exomeSeqStudiesCount = results.stream()
                                           .filter(e -> e.getKey().equals(EvaStudyBrowserTestData.EXOME_SEQUENCING))
                                           .mapToLong(Map.Entry::getValue).findAny().getAsLong();
        assertEquals(3, wgsStudiesCount);
        assertEquals(1, rnaSeqStudiesCount);
        assertEquals(2, exomeSeqStudiesCount);
    }

    @Test
    public void countFiles() throws Exception {
        QueryResult<Long> queryResult = archiveEvaproDBAdaptor.countFiles();

        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(3, queryResult.first().longValue());
    }

    @Test
    public void countSpecies() throws Exception {
        QueryResult<Long> queryResult = archiveEvaproDBAdaptor.countSpecies();

        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(7, queryResult.first().longValue());
    }

    @Test
    public void getBrowsableSpecies() throws Exception {
        QueryResult<Assembly> queryResult = archiveEvaproDBAdaptor.getBrowsableSpecies();

        assertEquals(2, queryResult.getNumTotalResults());
        // Those assemblies should not be returned by getBrowsableSpecies:
        // - Cow ones, because there are no 'loaded but not deleted' files
        // - Goat, Oarv40 for sheep because they are not loaded
        // - Pig, because the assembly code is emtpy
        // - Zebrafish, because the taxonomy code is empty
        // - Human, grch38 because the "loaded_assembly" field in browsable_file is null
        Set<String> assemblyCodes =
                queryResult.getResult().stream().map(Assembly::getAssemblyCode).collect(Collectors.toSet());
        assertEquals(new HashSet(Arrays.asList("galgal5", "oarv31")), assemblyCodes);
    }

    @Test
    public void getAccessionedSpecies() throws Exception {
        QueryResult<Assembly> queryResult = archiveEvaproDBAdaptor.getAccessionedSpecies();
        // Those assemblies should not be returned by getAccessionedSpecies:
        // - Chicken - galgal4, Sheep - Oar_v3.1 - assembly_in_accessioning_store is false
        // - Zebrafish - zv9, GRCz10 - Taxonomy code is empty
        // - Sscrofa11.1 - Pig - because the assembly code is emtpy
        assertEquals(4, queryResult.getNumTotalResults());
        Set<String> assemblyCodes =
                queryResult.getResult().stream().map(Assembly::getAssemblyCode).collect(Collectors.toSet());
        assertEquals(new HashSet<String>(Arrays.asList("galgal5", "oarv40", "grch38", "umd31")), assemblyCodes);
    }

}