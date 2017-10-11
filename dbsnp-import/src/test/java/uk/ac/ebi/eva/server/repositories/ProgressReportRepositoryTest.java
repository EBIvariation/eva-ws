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
import uk.ac.ebi.eva.server.models.Status;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ProgressReportRepositoryTest {

    @Autowired
    private ProgressReportRepository progressReportRepository;

    @Test
    public void testCountRecords() {
        assertEquals(80, progressReportRepository.count());
    }

    @Test
    public void testFindById() {
        ProgressReport report = progressReportRepository.findOne("fruitfly_7227");
        ProgressReport expected = new ProgressReport("fruitfly_7227", 7227, "Drosophila melanogaster",
                                                     "GCA_000001215.4", 149, true, false, false, Status.pending,
                                                     Status.pending, null, null);
        assertEquals(expected, report);
    }
}