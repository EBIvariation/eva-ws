/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.server.ws;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.eva.commons.core.models.AnnotationMetadata;
import uk.ac.ebi.eva.commons.mongodb.services.AnnotationMetadataService;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;

import java.util.List;

@RestController
@RequestMapping(value = "/v2/annotation-versions", produces = "application/json")
@Api(tags = {"annotation"})
public class AnnotationMetadataWSServerV2 {

    @Autowired
    private AnnotationMetadataService service;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public List<AnnotationMetadata> getAnnotationMetadata(@RequestParam(name = "species") String species,
                                                          @RequestParam(name = "assembly") String assembly)
            throws IllegalArgumentException {
        if (species.isEmpty()) {
            throw new IllegalArgumentException("Please specify a species");
        }
        if(assembly.isEmpty()) {
            throw new IllegalArgumentException("Please specify an assembly");
        }
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species + "_" + assembly));
        return service.findAllByOrderByCacheVersionDescVepVersionDesc();
    }

}
