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
package uk.ac.ebi.eva.lib.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.eva.lib.extension.ExtendedJpaRepositoryFunctions;
import uk.ac.ebi.eva.lib.entities.DgvaStudyBrowser;

@Transactional
public interface DgvaStudyBrowserRepository extends JpaRepository<DgvaStudyBrowser, String>, ExtendedJpaRepositoryFunctions<DgvaStudyBrowser, String>, JpaSpecificationExecutor<DgvaStudyBrowser> {

    public final static String COMMON_NAME = "commonName";
    public final static String STUDY_TYPE = "studyType";
    public final static String SCIENTIFIC_NAME = "scientificName";

}
