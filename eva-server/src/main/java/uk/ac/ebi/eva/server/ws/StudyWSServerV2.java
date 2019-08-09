package uk.ac.ebi.eva.server.ws;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/v2/studies", produces = "application/hal+json")
@Api(tags = "studies")
public class StudyWSServerV2 {

    @Autowired
    private VariantStudySummaryService variantStudySummaryService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity getBrowsableStudies(
            @ApiParam(value = "First letter of the genus, followed by the full species name, e.g. hsapiens. " +
                    "Allowed" + " values can be looked up in /v1/meta/species/list/ in the field named" +
                    " 'taxonomyCode'.", required = true)
            @RequestParam("species") String species,
            @ApiParam(value = "Encoded assembly name, e.g. grch37. Allowed values can be looked up in " +
                    "/v1/meta/species/list/ in the field named 'assemblyCode'.", required = true)
            @RequestParam("assembly") String assembly,
            @ApiParam(value = "The number of the page that shoulde be displayed. Starts from 0 and is an integer." +
                    " e.g. 0")
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @ApiParam(value = "The number of elements that should be displayed in a single page. e.g. 5")
            @RequestParam(required = false, defaultValue = "20") Integer pageSize)
            throws IllegalArgumentException {
        if (species == null || species.isEmpty()) {
            throw new IllegalArgumentException("Please specify a species");
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species + "_" + assembly));

        int totalNumberOfResults = variantStudySummaryService.countAll();
        if (totalNumberOfResults == 0) {
            return new ResponseEntity(new PagedResources<>(Collections.EMPTY_LIST, new PagedResources.PageMetadata
                    (pageSize, pageNumber < 0 ? 0 : pageNumber, totalNumberOfResults)), HttpStatus.NO_CONTENT);
        }

        List<VariantStudySummary> uniqueStudies = variantStudySummaryService.findAll(pageNumber, pageSize);

        PagedResources.PageMetadata pageMetadata;
        try {
            pageMetadata = buildPageMetadata(pageSize, pageNumber, totalNumberOfResults);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
        }

        PagedResources pagedResources = buildPagedResources(uniqueStudies, species, assembly, pageMetadata);
        return new ResponseEntity(pagedResources, HttpStatus.OK);
    }

    private PagedResources.PageMetadata buildPageMetadata(Integer pageSize, Integer pageNumber, Integer totalNumberOfResults)
            throws IllegalArgumentException {
        Long totalPages = pageSize == 0L ? 0L : (long) Math.ceil((double) totalNumberOfResults / (double) pageSize);

        if (pageNumber < 0 || pageNumber >= totalPages) {
            throw new IllegalArgumentException("For the given page size, there are " + totalPages + " page(s), so the" +
                    " correct page range is from 0 to " + String.valueOf(totalPages - 1) + " (both included).");
        }
        return new PagedResources.PageMetadata(pageSize, pageNumber, totalNumberOfResults, totalPages);
    }

    private PagedResources buildPagedResources(List<VariantStudySummary> uniqueStudies, String species,
                                               String assembly, PagedResources.PageMetadata pageMetadata) {

        PagedResources pagedResources = new PagedResources<>(uniqueStudies, pageMetadata);

        int pageNumber = (int) pageMetadata.getNumber();
        int pageSize = (int) pageMetadata.getSize();

        if (pageNumber > 0) {
            pagedResources.add(createPaginationLink(species, assembly, pageNumber - 1, pageSize, "prev"));

            pagedResources.add(createPaginationLink(species, assembly, 0, pageSize, "first"));
        }

        if (pageNumber < (pageMetadata.getTotalPages() - 1)) {
            pagedResources.add(createPaginationLink(species, assembly, pageNumber + 1, pageSize, "next"));

            pagedResources.add(createPaginationLink(species, assembly, (int) pageMetadata.getTotalPages() - 1, pageSize, "last"));
        }
        return pagedResources;
    }

    private Link createPaginationLink(String species, String assembly, int pageNumber, int pageSize, String linkName) {
        return new Link(linkTo(methodOn(StudyWSServerV2.class).getBrowsableStudies(species, assembly,
                pageNumber, pageSize))
                .toUriComponentsBuilder()
                .toUriString(), linkName);
    }
}
