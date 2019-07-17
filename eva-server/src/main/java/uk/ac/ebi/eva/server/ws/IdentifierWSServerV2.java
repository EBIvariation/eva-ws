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
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.AnnotationMetadataNotFoundException;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/v2/identifiers")
@Api(tags = "identifier")
public class IdentifierWSServerV2 {

    @Autowired
    private VariantWithSamplesAndAnnotationsService service;

    @GetMapping(value = "/{identifier}")
    public ResponseEntity getVariants(
            @ApiParam(value = "RS or SS identifier of a variant, e.g.: rs55880202", required = true) @PathVariable
                    String identifier,
            @ApiParam(value = "First letter of the genus, followed by the full species name, e.g. hsapiens. Allowed" +
                    " values can be looked up in /v1/meta/species/list/ in the field named 'taxonomyCode'.",
                    required = true) @RequestParam String species,
            @ApiParam(value = "Encoded assembly name, e.g. grch37. Allowed values can be looked up in" +
                    " /v1/meta/species/list/ in the field named 'assemblyCode'.", required = true)
            @RequestParam String assembly)
            throws AnnotationMetadataNotFoundException, IllegalArgumentException {
        checkParameters(species, assembly);

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species + "_" + assembly));
        List<VariantWithSamplesAndAnnotation> variantEntities = service.findByIdsAndComplexFilters(Arrays.asList
                (identifier), null, null, null, null);

        List<Variant> coreVariantInfo = new ArrayList<>();
        variantEntities.forEach(variantEntity -> {
            Variant variant = new Variant(variantEntity.getChromosome(), variantEntity.getStart(), variantEntity
                    .getEnd(), variantEntity.getReference(), variantEntity.getAlternate());
            variant.setIds(variantEntity.getIds());
            coreVariantInfo.add(variant);

        });
        if (coreVariantInfo.size() > 0) {
            return new ResponseEntity<>(coreVariantInfo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(coreVariantInfo, HttpStatus.NOT_FOUND);
        }
    }

    private void checkParameters(String species, String assembly) throws IllegalArgumentException {
        if (species.isEmpty()) {
            throw new IllegalArgumentException("Please specify a species");
        }
        if (assembly.isEmpty()) {
            throw new IllegalArgumentException("Please specify an assembly");
        }
    }
}
