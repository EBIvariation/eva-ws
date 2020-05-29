/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.server.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.server.models.ProgressReport;
import uk.ac.ebi.eva.server.models.ProgressReportPK;
import uk.ac.ebi.eva.server.models.Status;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ProgressReportRepositoryTest {

    @Autowired
    private ProgressReportRepository progressReportRepository;

    @Test
    public void testCountRecords() {
        assertEquals(81, progressReportRepository.count());
    }

    @Test
    public void testFindById() {
        Optional<ProgressReport> report = progressReportRepository.findById(new ProgressReportPK("fruitfly_7227",
                                                                                                 "GCA_000001215.4"));
        assertTrue(report.isPresent());
        ProgressReport expected = new ProgressReport("fruitfly_7227", 7227, "Drosophila melanogaster", "Fruit fly",
                                                     "GCA_000001215.4", 149, true, false, false, Status.pending,
                                                     Status.pending, Status.pending, null, null, null, 0L, 0L, 0L, 0L,
                                                     0L, 0L);
        assertEquals(expected, report.get());
    }

    @Test
    public void testFindMultipleAssemblies() {
        Iterable<ProgressReport> allReports = progressReportRepository.findAll();
        Set<String> ratAssemblies = new HashSet<>();
        for (ProgressReport r : allReports) {
            if (r.getCommonName().toLowerCase().equals("rat")) {
                ratAssemblies.add(r.getGenbankAssemblyAccession());
            }
        }
        assertEquals(3, ratAssemblies.size());
    }

    @Test
    public void testVariantWithEvidenceImportFields() {
        Optional<ProgressReport> report = progressReportRepository.findById(new ProgressReportPK("arabidopsis_3702",
                                                                                                 "GCA_000001735.1"));
        assertTrue(report.isPresent());
        assertEquals(Status.done, report.get().getVariantsWithEvidenceImported());
        Calendar cal = Calendar.getInstance();
        cal.set(2018, Calendar.MAY, 30, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        assertEquals(date.getTime(), report.get().getVariantsWithEvidenceImportedDate().getTime());
    }

    @Test
    public void testVariantWithoutGenbankAccession() {
        String databaseName = "orangutan_9600";
        String genbankAssemblyAccession = "";
        Optional<ProgressReport> report = progressReportRepository.findById(new ProgressReportPK(databaseName,
                                                                                                 genbankAssemblyAccession));
        assertTrue(report.isPresent());
        assertEquals(genbankAssemblyAccession, report.get().getGenbankAssemblyAccession());
        assertEquals(databaseName, report.get().getDatabaseName());
    }
}