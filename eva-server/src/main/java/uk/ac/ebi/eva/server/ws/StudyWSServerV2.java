package uk.ac.ebi.eva.server.ws;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.eva.commons.mongodb.entities.projections.VariantStudySummary;
import uk.ac.ebi.eva.commons.mongodb.services.VariantStudySummaryService;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;

import java.util.Collections;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/v2/studies", produces = "application/hal+json")
@Tag(name = "studies")
public class StudyWSServerV2 {

    @Autowired
    private VariantStudySummaryService variantStudySummaryService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity getBrowsableStudies(
            @Parameter(description = "First letter of the genus, followed by the full species name, e.g. hsapiens. " +
                    "Allowed values can be looked up in /v1/meta/species/list/ in the field named" +
                    " 'taxonomyCode'.", required = true)
            @RequestParam("species") String species,
            @Parameter(description = "Encoded assembly name, e.g. grch37. Allowed values can be looked up in " +
                    "/v1/meta/species/list/ in the field named 'assemblyCode'.", required = true)
            @RequestParam("assembly") String assembly,
            @Parameter(description = "The number of the page that should be displayed. Starts from 0 and is an integer." +
                    " e.g. 0")
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @Parameter(description = "The number of elements that should be displayed in a single page. e.g. 5")
            @RequestParam(required = false, defaultValue = "20") Integer pageSize)
            throws IllegalArgumentException {
        if (species == null || species.isEmpty()) {
            throw new IllegalArgumentException("Please specify a species");
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species + "_" + assembly));

        int totalNumberOfResults = variantStudySummaryService.countAll();
        if (totalNumberOfResults == 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(PagedModel.of(Collections.emptyList(),
                            new PagedModel.PageMetadata(
                                    pageSize.longValue(),
                                    pageNumber < 0 ? 0L : pageNumber.longValue(),
                                    totalNumberOfResults
                            )));
        }

        List<VariantStudySummary> uniqueStudies = variantStudySummaryService.findAll(pageNumber, pageSize);

        PagedModel.PageMetadata pageMetadata;
        try {
            pageMetadata = buildPageMetadata(pageSize, pageNumber, totalNumberOfResults);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
        }

        PagedModel<VariantStudySummary> pagedResources = buildPagedResources(uniqueStudies, species, assembly, pageMetadata);
        return new ResponseEntity(pagedResources, HttpStatus.OK);
    }

    private PagedModel.PageMetadata buildPageMetadata(Integer pageSize, Integer pageNumber, Integer
            totalNumberOfResults)
            throws IllegalArgumentException {
        Long totalPages = pageSize == 0L ? 0L : (long) Math.ceil((double) totalNumberOfResults / (double) pageSize);

        if (pageNumber < 0 || pageNumber >= totalPages) {
            throw new IllegalArgumentException("For the given page size, there are " + totalPages + " page(s), so " +
                    "the correct page range is from 0 to " + (totalPages - 1) + " (both included).");
        }
        return new PagedModel.PageMetadata(pageSize, pageNumber, totalNumberOfResults, totalPages);
    }

    private PagedModel<VariantStudySummary> buildPagedResources(List<VariantStudySummary> uniqueStudies, String species,
                                                                String assembly, PagedModel.PageMetadata pageMetadata) {

        PagedModel<VariantStudySummary> pagedResources = PagedModel.of(uniqueStudies, pageMetadata);

        int pageNumber = (int) pageMetadata.getNumber();
        int pageSize = (int) pageMetadata.getSize();

        if (pageNumber > 0) {
            pagedResources.add(createPaginationLink(species, assembly, pageNumber - 1, pageSize, "prev"));

            pagedResources.add(createPaginationLink(species, assembly, 0, pageSize, "first"));
        }

        if (pageNumber < (pageMetadata.getTotalPages() - 1)) {
            pagedResources.add(createPaginationLink(species, assembly, pageNumber + 1, pageSize, "next"));

            pagedResources.add(createPaginationLink(species, assembly, (int) pageMetadata.getTotalPages() - 1,
                    pageSize, "last"));
        }
        return pagedResources;
    }

    private Link createPaginationLink(String species, String assembly, int pageNumber, int pageSize, String linkName) {
        return linkTo(methodOn(StudyWSServerV2.class).getBrowsableStudies(species, assembly, pageNumber, pageSize))
                .withRel(linkName);
    }
}
