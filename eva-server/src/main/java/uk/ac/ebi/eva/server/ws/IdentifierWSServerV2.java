package uk.ac.ebi.eva.server.ws;

import io.swagger.annotations.Api;
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

    @GetMapping(value = "/{identifierId}")
    public ResponseEntity getVariants(@PathVariable("identifierId") String identifierId,
                                      @RequestParam(name = "species") String species,
                                      @RequestParam(name = "assembly") String assembly) throws
            AnnotationMetadataNotFoundException, IllegalArgumentException {
        checkParameters(species, assembly);

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species + "_" + assembly));
        List<VariantWithSamplesAndAnnotation> variantEntities = service.findByIdsAndComplexFilters(Arrays.asList
                (identifierId), null, null, null, null);

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
