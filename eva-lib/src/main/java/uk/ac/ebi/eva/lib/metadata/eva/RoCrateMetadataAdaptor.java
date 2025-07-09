package uk.ac.ebi.eva.lib.metadata.eva;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.ebi.eva.lib.entities.Project;
import uk.ac.ebi.eva.lib.models.rocrate.Dataset;
import uk.ac.ebi.eva.lib.models.rocrate.RoCrateEntity;
import uk.ac.ebi.eva.lib.models.rocrate.RoCrateMetadata;
import uk.ac.ebi.eva.lib.repositories.ProjectRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class RoCrateMetadataAdaptor {

    @Autowired
    private ProjectRepository projectRepository;

    public RoCrateMetadata getMetadataByProjectAccession(String accession) {
        Project project = projectRepository.getProjectByProjectAccession(accession);

        List<RoCrateEntity> entities = new ArrayList<>();
        // TODO get other properties... need: taxonomy, submission, analysis, file
        entities.add(new Dataset(project.getProjectAccession(), project.getTitle(), project.getDescription(),
                                 null, project.getCenterName(), null, null, null, null, null, project.getScope(),
                                 project.getMaterial(), project.getSourceType()));
        // TODO Create other RO-crate entities

        return new RoCrateMetadata(entities);
    }

}
