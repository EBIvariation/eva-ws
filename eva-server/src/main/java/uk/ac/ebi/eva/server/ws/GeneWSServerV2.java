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
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

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
    public ResponseEntity getVariantsByGene(
            @ApiParam(value = "Comma separated gene symbols and/or Ensembl gene IDs, e.g. BRCA2,FOXP2,ENSG00000223972")
            @PathVariable("geneIds") List<String> geneIds,
            @ApiParam(value = "First letter of the genus, followed by the full species name, e.g. hsapiens. Allowed" +
                    " values can be looked up in /v1/meta/species/list/ in the field named 'taxonomyCode'.",
                    required = true)
            @RequestParam(name = "species") String species,
            @ApiParam(value = "Encoded assembly name, e.g. grch37. Allowed values can be looked up in " +
                    "/v1/meta/species/list/ in the field named 'assemblyCode'.", required = true)
            @RequestParam(name = "assembly") String assembly,
            @ApiParam(value = "The number of the page that shoulde be displayed. Starts from 0 and is an integer.")
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @ApiParam(value = "The number of elements that should be retrieved per page.")
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            HttpServletResponse response,
            @ApiIgnore HttpServletRequest request)
            throws IllegalArgumentException {
        checkParameters(species, assembly);
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species + "_" + assembly));
        List<FeatureCoordinates> featureCoordinates = service.findAllByGeneIdsOrGeneNames(geneIds, geneIds);
        if (featureCoordinates.isEmpty()) {
            return new ResponseEntity(featureCoordinates, HttpStatus.NO_CONTENT);
        }

        String regions = featureCoordinates.stream().map(this::getRegionString).collect(Collectors.joining(","));

        ResponseEntity<PagedResources> responseEntity = regionWSServerV2.getVariantsByRegion(regions, species, assembly,
                null, null, null, null, null, null, null, pageNumber, pageSize, response, request);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        responseEntity.getBody().removeLinks();

        return new ResponseEntity(buildPage(geneIds, species, assembly, responseEntity.getBody(), response, request),
                HttpStatus.OK);
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

    private PagedResources buildPage(List<String> geneIds, String species, String assembly,
                                     PagedResources pagedResources, HttpServletResponse response,
                                     HttpServletRequest request) {

        int pageNumber = (int) pagedResources.getMetadata().getNumber();
        int pageSize = (int) pagedResources.getMetadata().getSize();
        int totalPages = (int) pagedResources.getMetadata().getTotalPages();

        if (pageNumber > 0) {
            pagedResources.add(createPaginationLink(geneIds, species, assembly, pageNumber - 1,
                    pageSize, response, request, "prev"));

            pagedResources.add(createPaginationLink(geneIds, species, assembly, 0, pageSize,
                    response, request, "first"));
        }

        if (pageNumber < (totalPages - 1)) {
            pagedResources.add(createPaginationLink(geneIds, species, assembly, pageNumber + 1,
                    pageSize, response, request, "next"));

            pagedResources.add(createPaginationLink(geneIds, species, assembly,
                    totalPages - 1, pageSize, response, request, "last"));
        }
        return pagedResources;
    }

    private Link createPaginationLink(List<String> geneIds, String species, String assembly, int pageNumber,
                                      int pageSize, HttpServletResponse response, HttpServletRequest request,
                                      String linkName) {
        return new Link(linkTo(methodOn(GeneWSServerV2.class).getVariantsByGene(geneIds, species, assembly,
                pageNumber, pageSize, response, request))
                .toUriComponentsBuilder()
                .toUriString(), linkName);
    }
}

