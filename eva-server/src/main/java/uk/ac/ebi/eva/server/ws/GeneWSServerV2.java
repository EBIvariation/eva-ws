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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.eva.commons.core.models.FeatureCoordinates;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigNamingConvention;
import uk.ac.ebi.eva.commons.mongodb.services.FeatureService;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.server.ws.contigalias.ContigAliasService;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/v2/genes", produces = "application/hal+json")
@Tag(name = "genes")
public class GeneWSServerV2 {

    @Autowired
    private FeatureService service;

    @Autowired
    private RegionWSServerV2 regionWSServerV2;

    @Autowired
    private ContigAliasService contigAliasService;

    public GeneWSServerV2() {
    }

    @GetMapping(value = "/{geneIds}/variants")
    public ResponseEntity getVariantsByGene(
            @Parameter(description = "Comma separated gene symbols and/or Ensembl gene IDs, e.g. BRCA2,FOXP2,ENSG00000223972")
            @PathVariable("geneIds") List<String> geneIds,
            @Parameter(description = "First letter of the genus, followed by the full species name, e.g. hsapiens. Allowed" +
                    " values can be looked up in /v1/meta/species/list/ in the field named 'taxonomyCode'.",
                    required = true)
            @RequestParam(name = "species") String species,
            @Parameter(description = "Encoded assembly name, e.g. grch37. Allowed values can be looked up in " +
                    "/v1/meta/species/list/ in the field named 'assemblyCode'.", required = true)
            @RequestParam(name = "assembly") String assembly,
            @Parameter(description = "Identifiers of studies. If this field is null/not specified, all studies should" +
                    " be queried. Each individual identifier of studies can be looked up in" +
                    " /v2/studies in the field named `studyId`. e.g. PRJEB6930,PRJEB27824")
            @RequestParam(name = "studies", required = false) List<String> studies,
            @Parameter(description = "Retrieve only variants with exactly this consequence type (as stated by Ensembl VEP)")
            @RequestParam(name = "annot-ct", required = false) List<String>
                    consequenceType,
            @Parameter(description = "Retrieve only variants whose Minor Allele Frequency is less than (<), less" +
                    " than or equals (<=), greater than (>), greater than or equals (>=) or equals (=) the" +
                    " provided number. e.g. <0.1")
            @RequestParam(name = "maf", required = false) String maf,
            @Parameter(description = "Retrieve only variants whose PolyPhen score as stated by Ensembl VEP is less than" +
                    " (<), less than or equals (<=), greater than (>), greater than or equals (>=) or equals (=) " +
                    "the provided number. e.g. <0.1")
            @RequestParam(name = "polyphen", required = false) String polyphenScore,
            @Parameter(description = "Retrieve only variants whose SIFT score as stated by Ensembl VEP is less than (<)," +
                    " less than or equals (<=), greater than (>), greater than or equals (>=) or equals (=) the " +
                    "provided number. e.g. <0.1")
            @RequestParam(name = "sift", required = false) String siftScore,
            @Parameter(description = "Ensembl VEP release whose annotations will be included in the response, e.g. 78")
            @RequestParam(name = "annot-vep-version", required = false) String
                    annotationVepVersion,
            @Parameter(description = "Ensembl VEP cache release whose annotations will be included in the response, " +
                    "e.g. 78")
            @RequestParam(name = "annot-vep-cache-version", required = false) String
                    annotationVepCacheVersion,
            @Parameter(description = "Contig naming convention desired, default is INSDC")
            @RequestParam(name = "contigNamingConvention", required = false) ContigNamingConvention contigNamingConvention,
            @Parameter(description = "The number of the page that should be displayed. Starts from 0 and is an integer.")
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @Parameter(description = "The number of elements that should be retrieved per page.")
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false, defaultValue = "0", name = "buffer") Integer bufferValue,
            HttpServletResponse response,
            @Parameter(hidden = true) HttpServletRequest request)
            throws IllegalArgumentException {
        checkParameters(geneIds, species, assembly, bufferValue);
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species + "_" + assembly));
        List<FeatureCoordinates> featureCoordinates = service.findAllByGeneIdsOrGeneNames(geneIds, geneIds);

        if (featureCoordinates.isEmpty()) {
            return new ResponseEntity(featureCoordinates, HttpStatus.NO_CONTENT);
        }

        if (bufferValue != 0) {
            List<FeatureCoordinates> bufferCoordinates = featureCoordinates.stream()
                    .map(coordinate -> new FeatureCoordinates(null, null, null, coordinate.getChromosome(),
                            coordinate.getStart() - bufferValue >= 0 ? coordinate.getStart() - bufferValue : 0,
                            coordinate.getEnd() + bufferValue)).collect(Collectors.toList());
            featureCoordinates = bufferCoordinates;
        }

        String regions = featureCoordinates.stream().map(this::getRegionString).collect(Collectors.joining(","));

        ResponseEntity<PagedModel<?>> responseEntity = regionWSServerV2.getVariantsByRegion(regions, species,
                assembly, studies, consequenceType, maf, polyphenScore, siftScore, annotationVepVersion,
                annotationVepCacheVersion, contigNamingConvention, pageNumber, pageSize, response, request);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        responseEntity.getBody().removeLinks();

        return new ResponseEntity(buildPage(geneIds, species, assembly, studies, consequenceType, maf, polyphenScore,
                siftScore, annotationVepVersion, annotationVepCacheVersion, contigNamingConvention, bufferValue,
                responseEntity.getBody(), response, request), HttpStatus.OK);
    }

    private void checkParameters(List<String> geneIds, String species, String assembly, Integer bufferValue)
            throws IllegalArgumentException {
        List<String> filteredGeneIds = geneIds.stream().filter(geneId -> geneId.isEmpty() == false).
                collect(Collectors.toList());
        geneIds.clear();
        geneIds.addAll(filteredGeneIds);

        if (geneIds.isEmpty()) {
            throw new IllegalArgumentException("Please specify geneIds");
        }

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

    private PagedModel<?> buildPage(List<String> geneIds, String species, String assembly, List<String> studies,
                                     List<String> consequenceType, String maf, String polyphenScore, String siftScore,
                                     String annotationVepVersion, String annotationVepCacheVersion,
                                     ContigNamingConvention contigNamingConvention, Integer bufferValue,
                                    PagedModel<?> pagedModel, HttpServletResponse response,
                                     HttpServletRequest request) {

        int pageNumber = (int) pagedModel.getMetadata().getNumber();
        int pageSize = (int) pagedModel.getMetadata().getSize();
        int totalPages = (int) pagedModel.getMetadata().getTotalPages();

        if (pageNumber > 0) {
            pagedModel.add(createPaginationLink(geneIds, species, assembly, studies, consequenceType,
                    maf, polyphenScore, siftScore, annotationVepVersion, annotationVepCacheVersion, contigNamingConvention,
                    pageNumber - 1, pageSize, bufferValue, response, request, "prev"));

            pagedModel.add(createPaginationLink(geneIds, species, assembly, studies, consequenceType,
                    maf, polyphenScore, siftScore, annotationVepVersion, annotationVepCacheVersion,
                    contigNamingConvention, 0, pageSize, bufferValue, response, request, "first"));
        }

        if (pageNumber < (totalPages - 1)) {
            pagedModel.add(createPaginationLink(geneIds, species, assembly, studies, consequenceType,
                    maf, polyphenScore, siftScore, annotationVepVersion, annotationVepCacheVersion,
                    contigNamingConvention, pageNumber + 1, pageSize, bufferValue, response, request,
                    "next"));

            pagedModel.add(createPaginationLink(geneIds, species, assembly, studies, consequenceType,
                    maf, polyphenScore, siftScore, annotationVepVersion, annotationVepCacheVersion,
                    contigNamingConvention, totalPages - 1, pageSize, bufferValue, response, request,
                    "last"));
        }
        return pagedModel;
    }

    private Link createPaginationLink(List<String> geneIds, String species, String assembly, List<String> studies,
                                      List<String> consequenceType, String maf, String polyphenScore, String siftScore,
                                      String annotationVepVersion, String annotationVepCacheVersion,
                                      ContigNamingConvention contigNamingConvention,
                                      int pageNumber, int pageSize, Integer bufferValue, HttpServletResponse response,
                                      HttpServletRequest request,
                                      String linkName) {
        return linkTo(methodOn(GeneWSServerV2.class)
                .getVariantsByGene(
                        geneIds, species, assembly, studies,
                        consequenceType, maf, polyphenScore, siftScore,
                        annotationVepVersion, annotationVepCacheVersion,
                        contigNamingConvention, pageNumber, pageSize,
                        bufferValue, response, request))
                .withRel(linkName);
    }
}

