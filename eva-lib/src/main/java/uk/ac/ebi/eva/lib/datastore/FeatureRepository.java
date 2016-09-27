/*
 * Copyright 2016 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.lib.datastore;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.eva.commons.models.data.FeatureCoordinates;

import java.util.List;

/**
 * This interface documents how features can be queried.
 *
 * This interface is used by Spring to create the query methods for features.
 * Spring creates the implementation automatically by looking at the method name.
 *
 * @author Jose Miguel Mut Lopez &lt;jmmut@ebi.ac.uk&gt;
 */
public interface FeatureRepository extends MongoRepository<FeatureCoordinates, String> {

    List<FeatureCoordinates> findByIdOrName(String id, String name);

}
