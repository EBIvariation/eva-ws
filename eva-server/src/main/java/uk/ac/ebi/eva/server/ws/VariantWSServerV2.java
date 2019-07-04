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
                                HttpServletResponse response) throws IllegalArgumentException{
        initializeQuery();
        try {
            checkParameters(variantCoreString, null, null, species, assembly);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new Resource<>(setErrorQueryResponse(e.getMessage()));
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species + "_" + assembly));

        List<VariantWithSamplesAndAnnotation> variantEntities;
        Long numTotalResults;

        try {
            variantEntities = getVariantEntitiesByParams(variantCoreString, null, null);
            numTotalResults = (long) variantEntities.size();
            if (numTotalResults == 0L) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return new Resource<>(setQueryResponse(variantEntities));
            }
        } catch (AnnotationMetadataNotFoundException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new Resource<>(setQueryResponse(ex.getMessage()));
        }

        List<Variant> variantList = new ArrayList<>();
        variantEntities.forEach(variantEntity->{
            Variant variant = new Variant(variantEntity.getChromosome(), variantEntity.getStart(),
                    variantEntity.getEnd(), variantEntity.getReference(),variantEntity.getAlternate());
            variant.setIds(variantEntity.getIds());
            variantList.add(variant);
        });

        QueryResult<Variant> queryResult = buildQueryResult(variantList, numTotalResults);

        Link annotationLink = new Link(linkTo(methodOn(VariantWSServerV2.class).getAnnotations(variantCoreString,
                species, assembly, null, null, response)).toUri().toString(), "annotation");

        Link sourcesLink = new Link(linkTo(methodOn(VariantWSServerV2.class).getsources(variantCoreString, species,
                assembly, null, null, response)).toUri().toString(), "sources");

        List<Link> links = new ArrayList<>();
        links.add(sourcesLink);
        links.add(annotationLink);
        return new Resource<>(setQueryResponse(queryResult), links);
    }

    private void checkParameters(String variantCoreString, String annotationVepVersion,
                                 String annotationVepCacheVersion, String species, String assembly)  {
        if (!variantCoreString.contains(":")) {
            throw new IllegalArgumentException("Please describe a variant as 'sequence:location:reference:alternate'");
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

    private List<VariantWithSamplesAndAnnotation> getVariantEntitiesByParams(String variantCoreString,
                                                                             String annotationVepVersion,
                                                                             String annotationVepCacheVersion) throws
            AnnotationMetadataNotFoundException {
        String[] regionId = variantCoreString.split(":");
        String alternate = (regionId.length > 3) ? regionId[3] : null;

        AnnotationMetadata annotationMetadata = null;
        if (annotationVepVersion != null && annotationVepCacheVersion != null) {
            annotationMetadata = new AnnotationMetadata(annotationVepVersion, annotationVepCacheVersion);
        }

        if (alternate != null) {
            return service.findByChromosomeAndStartAndReferenceAndAlternate(regionId[0], Integer.parseInt(regionId[1]),
                    regionId[2], alternate, annotationMetadata);
        } else {
            return service.findByChromosomeAndStartAndReference(regionId[0], Integer.parseInt(regionId[1]),
                    regionId[2], annotationMetadata);
        }
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

        List<VariantWithSamplesAndAnnotation> variantEntities;
        try {
            variantEntities = getVariantEntitiesByParams(variantCoreString, annotationVepVersion,
                    annotationVepCacheVersion);
            if (variantEntities.size() == 0) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return setQueryResponse(buildQueryResult(variantEntities, 0));
            }
        } catch (AnnotationMetadataNotFoundException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(ex.getMessage());
        }

        List<Annotation> annotations = new ArrayList<>();
        variantEntities.forEach(variantEntity -> {
            annotations.add(variantEntity.getAnnotation());
        });

        QueryResult<Annotation> queryResult = buildQueryResult(annotations, annotations.size());

        return setQueryResponse(queryResult);
    }

    @GetMapping(value = "/{variantCoreString}/source-entries")
    public QueryResponse getsources(@PathVariable("variantCoreString") String variantCoreString,
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

        List<VariantWithSamplesAndAnnotation> variantEntities;
        try {
            variantEntities = getVariantEntitiesByParams(variantCoreString, annotationVepVersion,
                    annotationVepCacheVersion);
            if (variantEntities.size() == 0) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return setQueryResponse(buildQueryResult(variantEntities, 0));
            }
        } catch (AnnotationMetadataNotFoundException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return setQueryResponse(ex.getMessage());
        }

        List<VariantSourceEntryWithSampleNames> variantSources = new ArrayList<>();
        variantEntities.forEach(variantEntity -> {
            variantEntity.getSourceEntries().forEach(sourceEntry -> {
                variantSources.add(sourceEntry);
            });
        });

        QueryResult<VariantSourceEntryWithSampleNames> queryResult = buildQueryResult(variantSources,
                variantSources.size());

        return setQueryResponse(queryResult);
    }
}
