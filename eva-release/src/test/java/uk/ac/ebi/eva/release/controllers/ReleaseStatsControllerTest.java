/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.release.controllers;

import org.assertj.core.util.IterableUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.release.Application;
import uk.ac.ebi.eva.release.dto.ReleaseStatsPerSpeciesDto;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
public class ReleaseStatsControllerTest {

    @Autowired
    private ReleaseStatsController releaseStatsController;

    @Test
    public void getAllSpeciesStats() {
        int totalNumberOfRecords = 417;
        Iterable<ReleaseStatsPerSpeciesDto> allRecords = releaseStatsController.getReleaseStatsPerSpecies(null, false);
        assertEquals(totalNumberOfRecords, IterableUtil.sizeOf(allRecords));
    }

    @Test
    public void getStatsByReleaseVersion() {
        int speciesInRelease2 = 218;
        Iterable<ReleaseStatsPerSpeciesDto> allRecords = releaseStatsController.getReleaseStatsPerSpecies(2, false);
        assertEquals(speciesInRelease2, IterableUtil.sizeOf(allRecords));
    }

    @Test
    public void getStatsByReleaseVersionNoUnmapped() {
        int speciesInRelease1ExcludingUnmapped = 58;
        Iterable<ReleaseStatsPerSpeciesDto> release1 = releaseStatsController.getReleaseStatsPerSpecies(1, true);
        assertEquals(speciesInRelease1ExcludingUnmapped, IterableUtil.sizeOf(release1));

        int speciesInRelease2ExcludingUnmapped = 79;
        Iterable<ReleaseStatsPerSpeciesDto> release2 = releaseStatsController.getReleaseStatsPerSpecies(2, true);
        assertEquals(speciesInRelease2ExcludingUnmapped, IterableUtil.sizeOf(release2));
    }

    @Test
    public void getStatsForNewVariantsOnlyInSpecificRelease() {
        int numberOfSpeciesWithNewVariantsRelease1 = 199;
        Iterable<ReleaseStatsPerSpeciesDto> Release1 = releaseStatsController.getSpeciesWithNewRsIds(1);
        assertEquals(numberOfSpeciesWithNewVariantsRelease1, IterableUtil.sizeOf(Release1));

        int numberOfSpeciesWithNewVariantsRelease2 = 29;
        Iterable<ReleaseStatsPerSpeciesDto> Release2 = releaseStatsController.getSpeciesWithNewRsIds(2);
        assertEquals(numberOfSpeciesWithNewVariantsRelease2, IterableUtil.sizeOf(Release2));
    }
}