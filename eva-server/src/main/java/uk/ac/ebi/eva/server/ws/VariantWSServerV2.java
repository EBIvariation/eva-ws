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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import uk.ac.ebi.eva.commons.core.models.Annotation;
import uk.ac.ebi.eva.commons.core.models.AnnotationMetadata;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.ws.VariantSourceEntryWithSampleNames;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.AnnotationMetadataNotFoundException;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/v2/variants", produces = "application/json")
@Api(tags = {"variants"})
public class VariantWSServerV2 extends EvaWSServer {

    @Autowired
    private VariantWithSamplesAndAnnotationsService service;

    @GetMapping(value = "/{variantCoreString}")
    public Resource getCoreInfo(@PathVariable("variantCoreString") String variantCoreString,
                                @RequestParam(name = "species") String species,
                                @RequestParam(name = "assembly") String assembly,
                                HttpServletResponse response) throws IllegalArgumentException {
        initializeQuery();
        try {
            checkParameters(variantCoreString, null, null, species, assembly);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new Resource<>(setErrorQueryResponse(e.getMessage()));
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species + "_" + assembly));

        Optional<VariantWithSamplesAndAnnotation> variantEntity;

        try {
            variantEntity = getVariantByCoordinatesAndAnnotationVersion(variantCoreString, null, null);
        } catch (AnnotationMetadataNotFoundException | IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new Resource<>(setQueryResponse(ex.getMessage()));
        }

        List<Variant> variantList = new ArrayList<>();
        if (!variantEntity.isPresent()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new Resource<>(setQueryResponse(variantList));
        }

        VariantWithSamplesAndAnnotation retrievedVariant = variantEntity.get();
        Variant variant = new Variant(retrievedVariant.getChromosome(), retrievedVariant.getStart(),
                retrievedVariant.getEnd(), retrievedVariant.getReference(), retrievedVariant.getAlternate());
        variant.setIds(variantEntity.get().getIds());
        variantList.add(variant);

        QueryResult<Variant> queryResult = buildQueryResult(variantList, 1);

        Link annotationLink = new Link(linkTo(methodOn(VariantWSServerV2.class).getAnnotations(variantCoreString,
                species, assembly, null, null, response)).toUri().toString(), "annotation");

        Link sourcesLink = new Link(linkTo(methodOn(VariantWSServerV2.class).getSources(variantCoreString, species,
                assembly, null, null, response)).toUri().toString(), "sources");

        List<Link> links = new ArrayList<>();
        links.add(sourcesLink);
        links.add(annotationLink);
        return new Resource<>(setQueryResponse(queryResult), links);
    }

    private void checkParameters(String variantCoreString, String annotationVepVersion,
                                 String annotationVepCacheVersion, String species, String assembly) {
        String[] regionId = variantCoreString.split(":");
        if (regionId.length != 4) {
            throw new IllegalArgumentException("Please describe a variant as 'sequence:location:reference:" +
                    "alternate'");
        }
        try {
            long position = Long.parseLong(regionId[1]);
            if (position < 0) {
                throw new IllegalArgumentException("Please provide a start position equals or greater than zero");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please specify a valid integer start position");
        }

        if (annotationVepVersion == null ^ annotationVepCacheVersion == null) {
            throw new IllegalArgumentException("Please specify either both annotation VEP version and annotation VEP" +
                    " cache version, or neither");
        }

        if (species.isEmpty()) {
            throw new IllegalArgumentException("Please specify a species");
        }

        if (assembly.isEmpty()) {
            throw new IllegalArgumentException("Please specify an assembly");
        }
    }

    private Optional<VariantWithSamplesAndAnnotation> getVariantByCoordinatesAndAnnotationVersion(
            String variantCoreString,
            String annotationVepVersion,
            String annotationVepCacheVersion)
            throws AnnotationMetadataNotFoundException, IllegalArgumentException {
        String[] regionId = variantCoreString.split(":");

        AnnotationMetadata annotationMetadata = null;
        if (annotationVepVersion != null && annotationVepCacheVersion != null) {
            annotationMetadata = new AnnotationMetadata(annotationVepVersion, annotationVepCacheVersion);
        }

        List<VariantWithSamplesAndAnnotation> variantWithSamplesAndAnnotationList = service.
                findByChromosomeAndStartAndReferenceAndAlternate(regionId[0], Integer.parseInt(regionId[1]),
                        regionId[2], regionId[3], annotationMetadata);

        if (variantWithSamplesAndAnnotationList.size() == 1) {
            return Optional.of(variantWithSamplesAndAnnotationList.get(0));
        } else if (variantWithSamplesAndAnnotationList.size() > 1) {
            throw new IllegalArgumentException("More than one variant has been found.");
        }
        return Optional.ofNullable(null);
    }

    @GetMapping(value = "/{variantCoreString}/annotations")
    public QueryResponse getAnnotations(@PathVariable("variantCoreString") String variantCoreString,
                                        @RequestParam(name = "species") String species,
                                        @RequestParam(name = "assembly") String assembly,
                                        @RequestParam(name = "annot-vep-version", required = false)
                                                String annotationVepVersion,
                                        @RequestParam(name = "annot-vep-cache-version", required = false)
                                                String annotationVepCacheVersion,
                                        HttpServletResponse response) throws IllegalArgumentException {
        initializeQuery();

        try {
            checkParameters(variantCoreString, annotationVepVersion, annotationVepCacheVersion,
                    species, assembly);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setErrorQueryResponse(e.getMessage());
        }
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species + "_" + assembly));

        Optional<VariantWithSamplesAndAnnotation> variantEntity;
        try {
            variantEntity = getVariantByCoordinatesAndAnnotationVersion(variantCoreString, annotationVepVersion,
                    annotationVepCacheVersion);
        } catch (AnnotationMetadataNotFoundException | IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(ex.getMessage());
        }

        List<Annotation> annotations = new ArrayList<>();
        if (!variantEntity.isPresent() || variantEntity.get().getAnnotation() == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return setQueryResponse(buildQueryResult(annotations, 0));
        }

        annotations.add(variantEntity.get().getAnnotation());

        QueryResult<Annotation> queryResult = buildQueryResult(annotations, annotations.size());

        return setQueryResponse(queryResult);
    }

    @GetMapping(value = "/{variantCoreString}/sources")
    public QueryResponse getSources(@PathVariable("variantCoreString") String variantCoreString,
                                    @RequestParam(name = "species") String species,
                                    @RequestParam(name = "assembly") String assembly,
                                    @RequestParam(name = "annot-vep-version", required = false)
                                            String annotationVepVersion,
                                    @RequestParam(name = "annot-vep-cache-version", required = false)
                                            String annotationVepCacheVersion,
                                    HttpServletResponse response) throws IllegalArgumentException {
        initializeQuery();

        try {
            checkParameters(variantCoreString, annotationVepVersion, annotationVepCacheVersion,
                    species, assembly);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setErrorQueryResponse(e.getMessage());
        }
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species + "_" + assembly));

        Optional<VariantWithSamplesAndAnnotation> variantEntity;
        try {
            variantEntity = getVariantByCoordinatesAndAnnotationVersion(variantCoreString, annotationVepVersion,
                    annotationVepCacheVersion);
        } catch (AnnotationMetadataNotFoundException | IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(ex.getMessage());
        }

        List<VariantSourceEntryWithSampleNames> variantSources = new ArrayList<>();
        if (!variantEntity.isPresent()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return setQueryResponse(buildQueryResult(variantSources, 0));
        }
        variantEntity.get().getSourceEntries().forEach(sourceEntry -> {
            variantSources.add(sourceEntry);
        });
        QueryResult<VariantSourceEntryWithSampleNames> queryResult = buildQueryResult(variantSources,
                variantSources.size());

        return setQueryResponse(queryResult);
    }
}
