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
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.PagedResources.PageMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;
import uk.ac.ebi.eva.commons.core.models.AnnotationMetadata;
import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.filter.FilterBuilder;
import uk.ac.ebi.eva.commons.mongodb.filter.VariantRepositoryFilter;
import uk.ac.ebi.eva.commons.mongodb.services.AnnotationMetadataNotFoundException;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.server.RateLimit;
import uk.ac.ebi.eva.server.Utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/v2/regions", produces = "application/hal+json")
@Api(tags = {"regions"})
public class RegionWSServerV2 {

    private static final int REGION_REQUEST_RATE_LIMIT = 5;

    @Autowired
    private VariantWithSamplesAndAnnotationsService service;

    public RegionWSServerV2() {
    }

    @GetMapping(value = "/{regionId}/variants")
    @ResponseBody
    @RateLimit(value = REGION_REQUEST_RATE_LIMIT)
    public ResponseEntity getVariantsByRegion(@PathVariable("regionId") String regionId,
                                              @RequestParam(name = "species") String species,
                                              @RequestParam(name = "assembly") String assembly,
                                              @RequestParam(name = "studies", required = false) List<String> studies,
                                              @RequestParam(name = "annot-ct", required = false) List<String>
                                                      consequenceType,
                                              @RequestParam(name = "maf", required = false) String maf,
                                              @RequestParam(name = "polyphen", required = false) String polyphenScore,
                                              @RequestParam(name = "sift", required = false) String siftScore,
                                              @RequestParam(name = "annot-vep-version", required = false) String
                                                      annotationVepVersion,
                                              @RequestParam(name = "annot-vep-cache-version", required = false) String
                                                      annotationVepCacheVersion,
                                              @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
                                              @RequestParam(required = false, defaultValue = "20") Integer pageSize,
                                              HttpServletResponse response,
                                              @ApiIgnore HttpServletRequest request)
            throws IllegalArgumentException {
        checkParameters(annotationVepVersion, annotationVepCacheVersion, species);

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species + "_" + assembly));

        List<VariantRepositoryFilter> filters = new FilterBuilder()
                .getVariantEntityRepositoryFilters(maf, polyphenScore, siftScore, studies, consequenceType);

        List<Region> regions = Region.parseRegions(regionId);
        List<String> excludeMapped = getExcludedFields();

        AnnotationMetadata annotationMetadata = getAnnotationMetadataHelper(annotationVepVersion,
                annotationVepCacheVersion);
        Integer totalNumberOfResults = service.countByRegionsAndComplexFilters(regions, filters).intValue();

        List<VariantWithSamplesAndAnnotation> variantEntities;
        List<Resource> resourcesList = new ArrayList<>();

        if (totalNumberOfResults == 0) {
            return new ResponseEntity(new PagedResources<>(resourcesList, new PageMetadata(pageSize,
                    pageNumber < 0 ? 0 : pageNumber, totalNumberOfResults)), HttpStatus.NO_CONTENT);
        }

        Long totalPages = pageSize == 0L ? 0L : (long) Math.ceil((double) totalNumberOfResults / (double) pageSize);

        if (pageNumber < 0 || pageNumber >= totalPages) {
            return new ResponseEntity("For the given page size, there are " + totalPages + " page(s), so the" +
                    " correct page range is from 0 to " + String.valueOf(totalPages - 1) + " (both included).",
                    HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
        }

        PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, pageNumber, totalNumberOfResults,
                totalPages);

        try {
            variantEntities = service.findByRegionsAndComplexFilters(regions,
                    filters,
                    annotationMetadata,
                    excludeMapped,
                    new PageRequest(pageNumber, pageSize));
        } catch (AnnotationMetadataNotFoundException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }

        variantEntities.forEach(variantEntity -> {
            Variant variant = new Variant(variantEntity.getChromosome(), variantEntity.getStart(),
                    variantEntity.getEnd(), variantEntity.getReference(), variantEntity.getAlternate());
            variant.setIds(variantEntity.getIds());
            variant.setMainId(variantEntity.getMainId());

            String variantCoreString = variantEntity.getChromosome() + ":" + variantEntity.getStart() + ":" +
                    variantEntity.getReference() + ":" + variantEntity.getAlternate();

            Link annotationsLink = new Link(linkTo(methodOn(VariantWSServerV2.class).getAnnotations(variantCoreString,
                    species, assembly, null, null, response)).toUri().toString(), "annotation");
            Link sourcesLink = new Link(linkTo(methodOn(VariantWSServerV2.class).getSources(variantCoreString, species,
                    assembly, null, null, response)).toUri().toString(), "sources");

            resourcesList.add(new Resource<>(variant, Arrays.asList(sourcesLink, annotationsLink)));
        });

        PagedResources pagedResources = new PagedResources<>(resourcesList, pageMetadata);

        if (pageNumber > 0) {
            pagedResources.add(createPaginationLink(regionId, species, assembly, studies, consequenceType, maf,
                    polyphenScore, siftScore, annotationVepVersion, annotationVepCacheVersion, pageNumber - 1,
                    pageSize, response, request, "prev"));

            pagedResources.add(createPaginationLink(regionId, species, assembly, studies, consequenceType, maf,
                    polyphenScore, siftScore, annotationVepVersion, annotationVepCacheVersion, 0, pageSize,
                    response, request, "first"));
        }

        if (pageNumber < (pageMetadata.getTotalPages() - 1)) {
            pagedResources.add(createPaginationLink(regionId, species, assembly, studies, consequenceType, maf,
                    polyphenScore, siftScore, annotationVepVersion, annotationVepCacheVersion, pageNumber + 1,
                    pageSize, response, request, "next"));

            pagedResources.add(createPaginationLink(regionId, species, assembly, studies, consequenceType, maf,
                    polyphenScore, siftScore, annotationVepVersion, annotationVepCacheVersion,
                    (int) pageMetadata.getTotalPages() - 1, pageSize, response, request, "last"));
        }
        return new ResponseEntity(pagedResources, HttpStatus.OK);
    }

    public String checkParameters(String annotationVepVersion, String annotationVepCacheVersion, String species) throws
            IllegalArgumentException {
        if (annotationVepVersion == null ^ annotationVepCacheVersion == null) {
            throw new IllegalArgumentException("Please specify either both annotation VEP version and annotation VEP" +
                    " cache version, or neither");
        }

        if (species.isEmpty()) {
            throw new IllegalArgumentException("Please specify a species");
        }
        return null;
    }

    public List<String> getExcludedFields() {
        List<String> excludeMapped = new ArrayList<>();
        Utils.getApiToMongoDocNameMap().forEach((key, value) -> {
            excludeMapped.add(value);
        });
        return excludeMapped;
    }

    public AnnotationMetadata getAnnotationMetadataHelper(String annotationVepVersion,
                                                          String annotationVepCacheVersion) {
        if (annotationVepVersion != null && annotationVepCacheVersion != null) {
            return new AnnotationMetadata(annotationVepVersion, annotationVepCacheVersion);
        }
        return null;
    }

    private Link createPaginationLink(String regionId, String species, String assembly, List<String> studies,
                                      List<String> consequenceType, String maf, String polyphenScore,
                                      String siftScore, String annotationVepVersion, String annotationVepCacheVersion,
                                      int pageNumber, int pageSize, HttpServletResponse response,
                                      HttpServletRequest request, String linkName) {
        return new Link(linkTo(methodOn(RegionWSServerV2.class).getVariantsByRegion(regionId, species,
                assembly, studies, consequenceType, maf, polyphenScore, siftScore, annotationVepVersion,
                annotationVepCacheVersion, pageNumber, pageSize, response, request))
                .toUriComponentsBuilder()
                .toUriString(), linkName);
    }
}
