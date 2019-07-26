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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import springfox.documentation.annotations.ApiIgnore;
import uk.ac.ebi.eva.commons.core.models.FeatureCoordinates;
import uk.ac.ebi.eva.commons.mongodb.services.FeatureService;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/v2/genes", produces = "application/hal+json")
@Api(tags = {"genes"})
public class GeneWSServerV2 {

    @Autowired
    private FeatureService service;

    @Autowired
    private RegionWSServerV2 regionWSServerV2;

    public GeneWSServerV2() {
    }

    @GetMapping(value = "/{geneIds}/variants")
    public ResponseEntity getVariantsByGene(@PathVariable("geneIds") List<String> geneIds,
                                            @RequestParam(name = "species") String species,
                                            @RequestParam(name = "assembly") String assembly,
                                            HttpServletResponse response,
                                            @ApiIgnore HttpServletRequest request)
            throws IllegalArgumentException {
        checkParameters(species, assembly);
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species + "_" + assembly));
        List<FeatureCoordinates> featureCoordinates = service.findAllByGeneIdsOrGeneNames(geneIds, geneIds);
        if (featureCoordinates.size() == 0) {
            return new ResponseEntity(featureCoordinates, HttpStatus.NO_CONTENT);
        }
        String regions = featureCoordinates.stream().map(this::getRegionString).collect(Collectors.joining(","));
        return regionWSServerV2.getVariantsByRegion(regions, species, assembly, null, null, null, null, null, null,
                null, response, request);
    }

    private void checkParameters(String species, String assembly) throws IllegalArgumentException {
        if (species.isEmpty()) {
            throw new IllegalArgumentException("Please specify a species");
        }

        if (assembly.isEmpty()) {
            throw new IllegalArgumentException("Please specify an assembly");
        }
    }

    private String getRegionString(FeatureCoordinates coordinates) {
        return coordinates.getChromosome() + ":" + coordinates.getStart() + "-" + coordinates.getEnd();
    }
}

