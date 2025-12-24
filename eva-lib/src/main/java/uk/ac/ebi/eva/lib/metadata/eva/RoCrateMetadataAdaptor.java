package uk.ac.ebi.eva.lib.metadata.eva;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.ebi.eva.lib.entities.Analysis;
import uk.ac.ebi.eva.lib.entities.DbXref;
import uk.ac.ebi.eva.lib.entities.File;
import uk.ac.ebi.eva.lib.entities.Project;
import uk.ac.ebi.eva.lib.entities.Sample;
import uk.ac.ebi.eva.lib.entities.Submission;
import uk.ac.ebi.eva.lib.entities.Taxonomy;
import uk.ac.ebi.eva.lib.models.rocrate.CommentEntity;
import uk.ac.ebi.eva.lib.models.rocrate.DataCatalogEntity;
import uk.ac.ebi.eva.lib.models.rocrate.MinimalProjectDatasetEntity;
import uk.ac.ebi.eva.lib.models.rocrate.ProjectDatasetEntity;
import uk.ac.ebi.eva.lib.models.rocrate.FileEntity;
import uk.ac.ebi.eva.lib.models.rocrate.LabProcessEntity;
import uk.ac.ebi.eva.lib.models.rocrate.Reference;
import uk.ac.ebi.eva.lib.models.rocrate.RoCrateEntity;
import uk.ac.ebi.eva.lib.models.rocrate.RoCrateMetadata;
import uk.ac.ebi.eva.lib.models.rocrate.SampleEntity;
import uk.ac.ebi.eva.lib.repositories.ProjectRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RoCrateMetadataAdaptor {

    protected static Logger logger = LoggerFactory.getLogger(RoCrateMetadataAdaptor.class);

    @Autowired
    private ProjectRepository projectRepository;

    public RoCrateMetadata getMetadataByProjectAccession(String accession) {
        Project project = projectRepository.getProjectByProjectAccession(accession);
        if (project == null) {
            return null;
        }

        // Construct the analysis-related entities
        List<RoCrateEntity> allAnalysisAdditionalProps = new ArrayList<>();
        List<RoCrateEntity> allFiles = new ArrayList<>();
        List<RoCrateEntity> allFileAdditionalProps = new ArrayList<>();
        List<RoCrateEntity> allSamples = new ArrayList<>();
        List<RoCrateEntity> analysisRoEntities = new ArrayList<>();
        List<Analysis> analyses = project.getAnalyses();
        for (Analysis analysis : analyses) {
            List<RoCrateEntity> analysisProps = getAdditionalAnalysisProperties(analysis);

            List<File> files = analysis.getFiles();
            List<RoCrateEntity> fileRoEntities = new ArrayList<>();
            List<RoCrateEntity> sampleRoEntities = new ArrayList<>();
            for (File file : files) {
                if (!file.getFileType().equalsIgnoreCase("VCF")) {
                    continue;
                }
                List<RoCrateEntity> fileProps = Collections.singletonList(
                        new CommentEntity(file.getFilename(), "md5", file.getFileMd5()));
                fileRoEntities.add(new FileEntity(project.getProjectAccession(), file.getFilename(), null,
                                                  file.getFileType(), getReferences(fileProps)));
                allFileAdditionalProps.addAll(fileProps);

                for (Map.Entry<String, Sample> entry : file.getNameInFileToSampleMap().entrySet()) {
                    sampleRoEntities.add(new SampleEntity(file.getFilename(), entry.getKey(), entry.getValue().getBiosampleAccession()));
                }
            }

            LocalDate analysisDate = analysis.getSubmission() != null ? analysis.getSubmission().getDate() : null;
            analysisRoEntities.add(new LabProcessEntity(analysis.getAnalysisAccession(), analysis.getTitle(),
                                                        analysis.getDescription(), analysisDate,
                                                        getReferences(sampleRoEntities), getReferences(fileRoEntities),
                                                        getReferences(analysisProps)));
            allAnalysisAdditionalProps.addAll(analysisProps);
            allFiles.addAll(fileRoEntities);
            allSamples.addAll(sampleRoEntities);
        }

        // Construct the DatasetEntity for the project and add all entities to the RO-crate metadata
        List<RoCrateEntity> entities = new ArrayList<>();
        List<String> publications = getPublications(project);
        List<RoCrateEntity> additionalProjectProperties = getAdditionalProjectProperties(project);
        entities.add(new ProjectDatasetEntity(project.getProjectAccession(), project.getTitle(), project.getDescription(),
                                       getFirstSubmissionDate(project), project.getCenterName(), publications,
                                       getReferences(analysisRoEntities), getReferences(allFiles),
                                       getReferences(additionalProjectProperties)));
        entities.addAll(additionalProjectProperties);
        entities.addAll(analysisRoEntities);
        entities.addAll(allAnalysisAdditionalProps);
        entities.addAll(allFiles);
        entities.addAll(allFileAdditionalProps);
        entities.addAll(allSamples);

        return new RoCrateMetadata(entities);
    }

    public RoCrateMetadata getAllProjects(){
        List<Project> projects = projectRepository.findAll();
        // Construct the project-related entities
        List<RoCrateEntity> projectsRoEntities = new ArrayList<>();
        List<RoCrateEntity> allAdditionalProjectProperties = new ArrayList<>();
        for (Project project : projects) {
            List<RoCrateEntity> additionalProjectProperties = getAdditionalProjectProperties(project);
            allAdditionalProjectProperties.addAll(additionalProjectProperties);
            projectsRoEntities.add(new MinimalProjectDatasetEntity(project.getProjectAccession(), project.getTitle(), project.getDescription(),
                    getFirstSubmissionDate(project), getReferences(additionalProjectProperties)));
        }

        // Construct the DataCatalogEntity and add all entities to the RO-crate metadata
        List<RoCrateEntity> entities = new ArrayList<>();
        entities.add(new DataCatalogEntity(getReferences(projectsRoEntities), getMostRecentDatePublished(projectsRoEntities)));
        entities.addAll(projectsRoEntities);
        entities.addAll(allAdditionalProjectProperties);
        return new RoCrateMetadata(entities);
    }

    private List<String> getPublications(Project project) {
        return project.getDbXrefs()
                      .stream()
                      .filter(dbXref -> dbXref.getLinkType().equalsIgnoreCase("publication"))
                      .map(DbXref::getCurie)
                      .collect(Collectors.toList());
    }

    private List<Reference> getReferences(List<RoCrateEntity> entities) {
        return entities.stream().map(entity -> new Reference(entity.getId())).collect(Collectors.toList());
    }

    private LocalDate getFirstSubmissionDate(Project project) {
        return project.getSubmissions().stream()
                      .min(Comparator.comparing(Submission::getDate, Comparator.nullsLast(LocalDate::compareTo)))
                      .map(Submission::getDate).orElse(null);
    }

    private LocalDate getMostRecentDatePublished(List<RoCrateEntity> projectsRoEntities) {
        return projectsRoEntities.stream()
                .map(entity -> (MinimalProjectDatasetEntity) entity)
                .max(Comparator.comparing(MinimalProjectDatasetEntity::getDatePublished,
                                          Comparator.nullsFirst(LocalDate::compareTo)))
                .map(MinimalProjectDatasetEntity::getDatePublished).orElse(null);
    }

    private List<RoCrateEntity> getAdditionalProjectProperties(Project project) {
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

        additionalProperties.add(new CommentEntity(project.getProjectAccession(), "taxonomyId", "" + taxonomyId));
        additionalProperties.add(new CommentEntity(project.getProjectAccession(), "scientificName", scientificName));
        additionalProperties.add(new CommentEntity(project.getProjectAccession(), "scope", project.getScope()));
        additionalProperties.add(new CommentEntity(project.getProjectAccession(), "material", project.getMaterial()));
        additionalProperties.add(new CommentEntity(project.getProjectAccession(), "sourceType", project.getSourceType()));

        return additionalProperties;
    }

    private List<RoCrateEntity> getAdditionalAnalysisProperties(Analysis analysis) {
        List<RoCrateEntity> additionalProperties = new ArrayList<>();
        String analysisAccession = analysis.getAnalysisAccession();
        additionalProperties.add(new CommentEntity(analysisAccession, "assemblyAccession",
                                                   analysis.getVcfReferenceAccession()));
        if (analysis.getExperimentType() != null) {
            additionalProperties.add(new CommentEntity(analysisAccession, "experimentType",
                                                       analysis.getExperimentType().getExperimentType()));
        }
        if (analysis.getPlatform() != null) {
            additionalProperties.add(new CommentEntity(analysisAccession, "platform",
                                                       analysis.getPlatform().getPlatform()));
        }
        return additionalProperties;
    }

}
