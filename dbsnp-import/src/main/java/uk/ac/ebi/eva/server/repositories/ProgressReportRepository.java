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

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import uk.ac.ebi.eva.server.models.ProgressReport;

@RepositoryRestResource(path = "progress")
public interface ProgressReportRepository extends PagingAndSortingRepository<ProgressReport, String> {

    // Prevents POST, PUT and PATCH
    @Override
    @RestResource(exported = false)
    ProgressReport save(ProgressReport s);

    // Prevents DELETE
    @Override
    @RestResource(exported = false)
    void delete(ProgressReport t);

}
