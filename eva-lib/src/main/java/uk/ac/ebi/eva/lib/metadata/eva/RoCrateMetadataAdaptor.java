package uk.ac.ebi.eva.lib.metadata.eva;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.ebi.eva.lib.entities.DbXref;
import uk.ac.ebi.eva.lib.entities.Project;
import uk.ac.ebi.eva.lib.entities.Taxonomy;
import uk.ac.ebi.eva.lib.models.rocrate.CommentEntity;
import uk.ac.ebi.eva.lib.models.rocrate.DatasetEntity;
import uk.ac.ebi.eva.lib.models.rocrate.Reference;
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

        // TODO use entities/references for publications too?
        List<String> publications = project.getDbXrefs()
                                           .stream()
                                           .filter(dbXref -> dbXref.getLinkType().equalsIgnoreCase("publication"))
                                           .map(DbXref::getCurie)
                                           .collect(Collectors.toList());
        List<RoCrateEntity> additionalProperties = getAdditionalProperties(project);
        // TODO add submission date

        List<RoCrateEntity> entities = new ArrayList<>();
        entities.add(new DatasetEntity(project.getProjectAccession(), project.getTitle(), project.getDescription(),
                                       null, project.getCenterName(), publications, null, null,
                                       getReferences(additionalProperties)));
        entities.addAll(additionalProperties);
        // TODO Create and reference other RO-crate entities: analysis, file, sample

        return new RoCrateMetadata(entities);
    }

    private List<Reference> getReferences(List<RoCrateEntity> entities) {
        return entities.stream().map(entity -> new Reference(entity.getId())).collect(Collectors.toList());
    }

    private List<RoCrateEntity> getAdditionalProperties(Project project) {
        List<RoCrateEntity> additionalProperties = new ArrayList<>();
        Long taxonomyId = null;
        String scientificName = null;
        if (project.getTaxonomies().isEmpty()) {
            logger.warn("No taxonomies found for project {}", project.getProjectAccession());
        } else {
            if (project.getTaxonomies().size() > 1) {
                logger.warn("Multiple taxonomies for project {}, will use the first", project.getProjectAccession());
            }
            Taxonomy taxonomy = project.getTaxonomies().get(0);
            taxonomyId = taxonomy.getTaxonomyId();
            scientificName = taxonomy.getScientificName();
        }

        additionalProperties.add(new CommentEntity("taxonomyId", "" + taxonomyId));
        additionalProperties.add(new CommentEntity("scientificName", scientificName));
        additionalProperties.add(new CommentEntity("scope", project.getScope()));
        additionalProperties.add(new CommentEntity("material", project.getMaterial()));
        additionalProperties.add(new CommentEntity("sourceType", project.getSourceType()));

        return additionalProperties;
    }

}
