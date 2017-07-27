/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014-2016 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.dgva.server.ws;

import io.swagger.annotations.Api;
import org.opencb.datastore.core.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.lib.metadata.StudyDgvaDBAdaptor;
import uk.ac.ebi.eva.lib.utils.QueryUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@RestController
@RequestMapping(value = "/v1/meta", produces = "application/json")
@Api(tags = {"archive"})
public class ArchiveWSServer extends DgvaWSServer {
    @Autowired
    private StudyDgvaDBAdaptor studyDgvaDbAdaptor;

    private Properties properties;
    
    public ArchiveWSServer() throws IOException {
    }

    @RequestMapping(value = "/studies/all", method = RequestMethod.GET)
    public QueryResponse getStudies(@RequestParam(name = "species", required = false) String species,
                                    @RequestParam(name = "type", required = false) String types) {
        initializeQuery();
        if (species != null && !species.isEmpty()) {
            getQueryOptions().put("species", Arrays.asList(species.split(",")));
        }
        if (types != null && !types.isEmpty()) {
            getQueryOptions().put("type", Arrays.asList(types.split(",")));
        }

        return setQueryResponse(studyDgvaDbAdaptor.getAllStudies(getQueryOptions()));
    }
}
