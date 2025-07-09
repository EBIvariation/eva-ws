package uk.ac.ebi.eva.lib.metadata.eva;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.ebi.eva.lib.entities.DbXref;
import uk.ac.ebi.eva.lib.entities.Project;
import uk.ac.ebi.eva.lib.entities.Taxonomy;
import uk.ac.ebi.eva.lib.models.rocrate.Dataset;
import uk.ac.ebi.eva.lib.models.rocrate.RoCrateEntity;
import uk.ac.ebi.eva.lib.models.rocrate.RoCrateMetadata;
import uk.ac.ebi.eva.lib.repositories.ProjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoCrateMetadataAdaptor {

    protected static Logger logger = LoggerFactory.getLogger(RoCrateMetadataAdaptor.class);

    @Autowired
    private ProjectRepository projectRepository;

    public RoCrateMetadata getMetadataByProjectAccession(String accession) {
        Project project = projectRepository.getProjectByProjectAccession(accession);
        Long taxonomyId = null;
        String scientificName = null;
        if (project.getTaxonomies().isEmpty()) {
            logger.warn("No taxonomies found for project {}", accession);
        } else {
            if (project.getTaxonomies().size() > 1) {
                logger.warn("Multiple taxonomies for project {}, will use the first", accession);
            }
            Taxonomy taxonomy = project.getTaxonomies().get(0);
            taxonomyId = taxonomy.getTaxonomyId();
            scientificName = taxonomy.getScientificName();
        }
        List<String> publications = project.getDbXrefs()
                                           .stream()
                                           .filter(dbXref -> dbXref.getLinkType().equalsIgnoreCase("publication"))
                                           .map(DbXref::getCurie)
                                           .collect(Collectors.toList());

        List<RoCrateEntity> entities = new ArrayList<>();
        // TODO Get properties from: submission, analysis, file
        entities.add(new Dataset(project.getProjectAccession(), project.getTitle(), project.getDescription(),
                                 null, project.getCenterName(), publications, null, null, taxonomyId, scientificName,
                                 project.getScope(),
                                 project.getMaterial(), project.getSourceType()));
        // TODO Create other RO-crate entities

        return new RoCrateMetadata(entities);
    }

}
