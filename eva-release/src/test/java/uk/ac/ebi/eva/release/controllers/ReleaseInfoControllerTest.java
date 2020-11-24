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
import uk.ac.ebi.eva.release.models.ReleaseInfo;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
public class ReleaseInfoControllerTest {

    @Autowired
    private ReleaseInfoController releaseInfoController;

    @Test
    public void getOneReleaseInfo() {
        int releaseVersion = 1;
        Iterable<ReleaseInfo> releaseInfo = releaseInfoController.getReleaseInfo(releaseVersion);
        assertEquals(releaseVersion, releaseInfo.iterator().next().getReleaseVersion());
    }

    @Test
    public void getAllReleasesInfo() {
        Integer releaseVersion = null;
        Iterable<ReleaseInfo> releaseInfo = releaseInfoController.getReleaseInfo(releaseVersion);
        assertEquals(2, IterableUtil.sizeOf(releaseInfo));
    }

    @Test
    public void getLatestReleaseInfo() {
        int latestReleaseVersion = 2;
        ReleaseInfo latestReleaseInfo = releaseInfoController.getLatestReleaseInfo();
        assertEquals(latestReleaseVersion, latestReleaseInfo.getReleaseVersion());
    }
}