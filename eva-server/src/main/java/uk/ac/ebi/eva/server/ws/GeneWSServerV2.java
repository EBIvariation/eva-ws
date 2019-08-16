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
import java.util.ArrayList;
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
            @ApiParam(value = "Identifiers of studies. If this field is null/not specified, all studies should" +
                    " be queried. Each individual identifier of studies can be looked up in" +
                    " /v2/studies in the field named `studyId`. e.g. PRJEB6930,PRJEB27824")
            @RequestParam(name = "studies", required = false) List<String> studies,
            @ApiParam(value = "Retrieve only variants with exactly this consequence type (as stated by Ensembl VEP)")
            @RequestParam(name = "annot-ct", required = false) List<String>
                    consequenceType,
            @ApiParam(value = "Retrieve only variants whose Minor Allele Frequency is less than (<), less" +
                    " than or equals (<=), greater than (>), greater than or equals (>=) or equals (=) the" +
                    " provided number. e.g. <0.1")
            @RequestParam(name = "maf", required = false) String maf,
            @ApiParam(value = "Retrieve only variants whose PolyPhen score as stated by Ensembl VEP is less than" +
                    " (<), less than or equals (<=), greater than (>), greater than or equals (>=) or equals (=) " +
                    "the provided number. e.g. <0.1")
            @RequestParam(name = "polyphen", required = false) String polyphenScore,
            @ApiParam(value = "Retrieve only variants whose SIFT score as stated by Ensembl VEP is less than (<)," +
                    " less than or equals (<=), greater than (>), greater than or equals (>=) or equals (=) the " +
                    "provided number. e.g. <0.1")
            @RequestParam(name = "sift", required = false) String siftScore,
            @ApiParam(value = "Ensembl VEP release whose annotations will be included in the response, e.g. 78")
            @RequestParam(name = "annot-vep-version", required = false) String
                    annotationVepVersion,
            @ApiParam(value = "Ensembl VEP cache release whose annotations will be included in the response, " +
                    "e.g. 78")
            @RequestParam(name = "annot-vep-cache-version", required = false) String
                    annotationVepCacheVersion,
            @ApiParam(value = "The number of the page that should be displayed. Starts from 0 and is an integer.")
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @ApiParam(value = "The number of elements that should be retrieved per page.")
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false, defaultValue = "0", name = "buffer") Integer bufferValue,
            HttpServletResponse response,
            @ApiIgnore HttpServletRequest request)
            throws IllegalArgumentException {
        checkParameters(species, assembly, bufferValue);
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species + "_" + assembly));
        List<FeatureCoordinates> featureCoordinates = service.findAllByGeneIdsOrGeneNames(geneIds, geneIds);

        if (featureCoordinates.isEmpty()) {
            return new ResponseEntity(featureCoordinates, HttpStatus.NO_CONTENT);
        }

        if (bufferValue != 0) {
            List<FeatureCoordinates> bufferCoordinates = new ArrayList<>();
            featureCoordinates.forEach(coordinate -> {
                bufferCoordinates.add(new FeatureCoordinates(null, null, null, coordinate.getChromosome(),
                        coordinate.getStart() - bufferValue >= 0 ? coordinate.getStart() - bufferValue : 0,
                        coordinate.getEnd() + bufferValue));
            });
            featureCoordinates = bufferCoordinates;
        }

        String regions = featureCoordinates.stream().map(this::getRegionString).collect(Collectors.joining(","));

        ResponseEntity<PagedResources> responseEntity = regionWSServerV2.getVariantsByRegion(regions, species,
                assembly, studies, consequenceType, maf, polyphenScore, siftScore, annotationVepVersion,
                annotationVepCacheVersion, pageNumber, pageSize, response, request);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        responseEntity.getBody().removeLinks();

        return new ResponseEntity(buildPage(geneIds, species, assembly, studies, consequenceType, maf, polyphenScore,
                siftScore, annotationVepVersion, annotationVepCacheVersion, bufferValue, responseEntity.getBody(),
                response, request), HttpStatus.OK);
    }

    private void checkParameters(String species, String assembly, Integer bufferValue) throws IllegalArgumentException {
        if (species.isEmpty()) {
            throw new IllegalArgumentException("Please specify a species");
        }

        if (assembly.isEmpty()) {
            throw new IllegalArgumentException("Please specify an assembly");
        }

        if (bufferValue < 0) {
            throw new IllegalArgumentException("Pleas specify a non-negative integer value for buffer");
        }
    }

    private String getRegionString(FeatureCoordinates coordinates) {
        return coordinates.getChromosome() + ":" + coordinates.getStart() + "-" + coordinates.getEnd();
    }

    private PagedResources buildPage(List<String> geneIds, String species, String assembly, List<String> studies,
                                     List<String> consequenceType, String maf, String polyphenScore, String siftScore,
                                     String annotationVepVersion, String annotationVepCacheVersion,
                                     Integer bufferValue, PagedResources pagedResources,
                                     HttpServletResponse response, HttpServletRequest request) {

        int pageNumber = (int) pagedResources.getMetadata().getNumber();
        int pageSize = (int) pagedResources.getMetadata().getSize();
        int totalPages = (int) pagedResources.getMetadata().getTotalPages();

        if (pageNumber > 0) {
            pagedResources.add(createPaginationLink(geneIds, species, assembly, studies, consequenceType,
                    maf, polyphenScore, siftScore, annotationVepVersion, annotationVepCacheVersion,
                    pageNumber - 1, pageSize, bufferValue, response, request, "prev"));

            pagedResources.add(createPaginationLink(geneIds, species, assembly, studies, consequenceType,
                    maf, polyphenScore, siftScore, annotationVepVersion, annotationVepCacheVersion,
                    pageNumber - 1, pageSize, bufferValue, response, request, "first"));
        }

        if (pageNumber < (totalPages - 1)) {
            pagedResources.add(createPaginationLink(geneIds, species, assembly, studies, consequenceType,
                    maf, polyphenScore, siftScore, annotationVepVersion, annotationVepCacheVersion,
                    pageNumber - 1, pageSize, bufferValue, response, request, "next"));

            pagedResources.add(createPaginationLink(geneIds, species, assembly, studies, consequenceType,
                    maf, polyphenScore, siftScore, annotationVepVersion, annotationVepCacheVersion,
                    pageNumber - 1, pageSize, bufferValue, response, request, "last"));
        }
        return pagedResources;
    }

    private Link createPaginationLink(List<String> geneIds, String species, String assembly, List<String> studies,
                                      List<String> consequenceType, String maf, String polyphenScore, String siftScore,
                                      String annotationVepVersion, String annotationVepCacheVersion,
                                      int pageNumber, int pageSize, Integer bufferValue, HttpServletResponse response,
                                      HttpServletRequest request,
                                      String linkName) {
        return new Link(linkTo(methodOn(GeneWSServerV2.class).getVariantsByGene(geneIds, species, assembly, studies,
                consequenceType, maf, polyphenScore, siftScore, annotationVepVersion,
                annotationVepCacheVersion, pageNumber, pageSize, bufferValue, response, request))
                .toUriComponentsBuilder()
                .toUriString(), linkName);
    }
}

