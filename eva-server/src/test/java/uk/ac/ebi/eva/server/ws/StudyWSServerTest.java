package uk.ac.ebi.eva.server.ws;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.lib.entities.Analysis;
import uk.ac.ebi.eva.lib.entities.DbXref;
import uk.ac.ebi.eva.lib.entities.ExperimentType;
import uk.ac.ebi.eva.lib.entities.File;
import uk.ac.ebi.eva.lib.entities.FileSample;
import uk.ac.ebi.eva.lib.entities.FileSamplePK;
import uk.ac.ebi.eva.lib.entities.Platform;
import uk.ac.ebi.eva.lib.entities.Project;
import uk.ac.ebi.eva.lib.entities.Sample;
import uk.ac.ebi.eva.lib.entities.Submission;
import uk.ac.ebi.eva.lib.entities.Taxonomy;
import uk.ac.ebi.eva.lib.models.rocrate.CommentEntity;
import uk.ac.ebi.eva.lib.models.rocrate.DataCatalogEntity;
import uk.ac.ebi.eva.lib.models.rocrate.DatasetMinimalProjectEntity;
import uk.ac.ebi.eva.lib.models.rocrate.DatasetProjectEntity;
import uk.ac.ebi.eva.lib.models.rocrate.FileEntity;
import uk.ac.ebi.eva.lib.models.rocrate.LabProcessEntity;
import uk.ac.ebi.eva.lib.models.rocrate.MetadataEntity;
import uk.ac.ebi.eva.lib.models.rocrate.Reference;
import uk.ac.ebi.eva.lib.models.rocrate.RoCrateEntity;
import uk.ac.ebi.eva.lib.models.rocrate.RoCrateMetadata;
import uk.ac.ebi.eva.lib.models.rocrate.SampleEntity;
import uk.ac.ebi.eva.lib.repositories.AnalysisRepository;
import uk.ac.ebi.eva.lib.repositories.DbXrefRepository;
import uk.ac.ebi.eva.lib.repositories.ExperimentTypeRepository;
import uk.ac.ebi.eva.lib.repositories.FileRepository;
import uk.ac.ebi.eva.lib.repositories.FileSampleRepository;
import uk.ac.ebi.eva.lib.repositories.PlatformRepository;
import uk.ac.ebi.eva.lib.repositories.ProjectRepository;
import uk.ac.ebi.eva.lib.repositories.SampleRepository;
import uk.ac.ebi.eva.lib.repositories.SubmissionRepository;
import uk.ac.ebi.eva.lib.repositories.TaxonomyRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudyWSServerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaxonomyRepository taxonomyRepository;

    @Autowired
    private DbXrefRepository dbXrefRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private SampleRepository sampleRepository;

    @Autowired
    private FileSampleRepository fileSampleRepository;

    @Autowired
    private ExperimentTypeRepository experimentTypeRepository;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Before
    public void setUp() {
        // Create and save entities required for analyses
        Submission submission1 = new Submission(1L, "ERA123", "PROJECT", "ADD", "first submission", null,
                                                LocalDate.of(2025, 1, 1), 1);
        Submission submission2 = new Submission(2L, "ERA456", "PROJECT", "ADD", "second submission", null,
                                                LocalDate.of(2025, 7, 1), 1);
        List<Submission> submissions = Arrays.asList(submission1, submission2);
        submissionRepository.saveAll(submissions);

        Platform platform = new Platform(1L, "Illumina HiSeq 2000", "Illumina");
        ExperimentType experimentType = new ExperimentType(1L, "Whole genome sequencing");
        platformRepository.save(platform);
        experimentTypeRepository.save(experimentType);

        // File in analysis 1 with 2 samples
        File file1 = new File(1L, "ena file id", "file1.vcf", "md5_1", "file location", "VCF", "submitted", 1,
                              true, null, false, "eva file id");
        // File in analysis 2 with 1 sample
        File file2 = new File(2L, "ena file id", "file2.vcf", "md5_2", "file location", "vcf", "submitted", 1,
                              true, null, false, "eva file id");
        // Non-VCF file in analysis 1
        File file3 = new File(3L, "ena file id", "file1.vcf.csi", "md5_3", "file location", "csi", "submitted", 1,
                              true, null, false, "eva file id");
        // VCF file in analysis 1 with no samples
        File file4 = new File(4L, "ena file id", "file4.vcf", "md5_4", "file location", "VCF", "submitted", 1,
                              true, null, false, "eva file id");
        Sample sample1 = new Sample(1L, "SAMEA0001", "ERS0001");
        Sample sample2 = new Sample(2L, "SAMEA0002", "ERS0002");
        sampleRepository.saveAll(Arrays.asList(sample1, sample2));
        fileRepository.saveAll(Arrays.asList(file1, file2, file3, file4));

        FileSample fileSample11 = new FileSample(file1, sample1, "sample1_in_file1");
        FileSample fileSample12 = new FileSample(file1, sample2, "sample2_in_file1");
        FileSample fileSample22 = new FileSample(file2, sample2, "sample2_in_file2");
        file1.setFileSamples(Arrays.asList(fileSample11, fileSample12));
        file2.setFileSamples(Collections.singletonList(fileSample22));
        sample1.setFileSamples(Collections.singletonList(fileSample11));
        sample2.setFileSamples(Arrays.asList(fileSample12, fileSample22));
        fileSample11.setFileSamplePK(new FileSamplePK(1L, 1L));
        fileSample12.setFileSamplePK(new FileSamplePK(1L, 2L));
        fileSample22.setFileSamplePK(new FileSamplePK(2L, 2L));
        fileSampleRepository.saveAll(Arrays.asList(fileSample11, fileSample12, fileSample22));

        Analysis analysis1 = new Analysis("ERZ0001", "analysis 1", "alias", "description", "center", null, "reference",
                                          "GCA_0001.1", 0, 1L);
        Analysis analysis2 = new Analysis("ERZ0002", "analysis 2", "alias", "description", "center", null, "reference",
                                          "GCA_0001.2", 0, 1L);
        List<Analysis> analyses = Arrays.asList(analysis1, analysis2);
        analysis1.setExperimentType(experimentType);
        analysis1.setPlatform(platform);
        analysis1.setSubmission(submission1);
        analysis1.setFiles(Arrays.asList(file1, file3, file4));
        analysis2.setFiles(Collections.singletonList(file2));
        // Analysis 2 missing experiment type, platform and submissions
        analysisRepository.saveAll(analyses);

        // Create and save entities required for project
        Taxonomy taxonomy = new Taxonomy(9606L, "human", "Homo sapiens", "hsapiens", "human");
        DbXref dbXref = new DbXref(1L, "doi", "123", null, "publication", "project");
        taxonomyRepository.save(taxonomy);
        dbXrefRepository.save(dbXref);

        Project project = new Project("PRJEB0001", "submitter center", "project alias", "project title", "description",
                                      "multi-isolate", "DNA", "other", "other", "studyId", "germline", 999L, null, null,
                                      null, null, "control set");

        project.setDbXrefs(Collections.singletonList(dbXref));
        project.setTaxonomies(Collections.singletonList(taxonomy));
        project.setSubmissions(submissions);
        project.setAnalyses(analyses);
        projectRepository.save(project);
    }

    @Test
    public void testGetRoCrate() {
        String url = "/v1/studies/ro-crate/PRJEB0001";
        ResponseEntity<RoCrateMetadata> response = restTemplate.exchange(url, HttpMethod.GET, null,
                                                                         RoCrateMetadata.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        RoCrateMetadata roCrateMetadata = response.getBody();

        // First entity describes the metadata document itself
        MetadataEntity metadata = (MetadataEntity) roCrateMetadata.getGraph().get(0);
        assertEquals("ro-crate-metadata.json", metadata.getId());

        // Second entity is the dataset, corresponding to the project
        DatasetProjectEntity dataset = (DatasetProjectEntity) roCrateMetadata.getGraph().get(1);
        assertEquals("PRJEB0001", dataset.getProjectAccession());
        assertEquals("project title", dataset.getName());
        assertEquals(LocalDate.of(2025, 1, 1), dataset.getDatePublished());

        // Additional properties of project are separate entities
        List<Reference> taxonomyRefs = dataset.getAdditionalProperties()
                                              .stream()
                                              .filter(ref -> ref.getId().equalsIgnoreCase("#taxonomyId"))
                                              .collect(Collectors.toList());
        List<RoCrateEntity> taxonomyEntities = roCrateMetadata.getEntities(taxonomyRefs);
        assertEquals(1, taxonomyEntities.size());
        assertEquals("9606", ((CommentEntity) taxonomyEntities.get(0)).getText());

        // Check analysis entities (LabProcess)
        List<RoCrateEntity> analyses = roCrateMetadata.getEntitiesOfType("LabProcess");
        assertEquals(2, analyses.size());
        LabProcessEntity analysis = (LabProcessEntity) analyses.stream().sorted().findFirst().get();
        assertEquals("ERZ0001", analysis.getAnalysisAccession());

        // Check file entities
        List<RoCrateEntity> fileEntities = roCrateMetadata.getEntities(analysis.getFiles());
        assertEquals(2, fileEntities.size());
        FileEntity file = (FileEntity) fileEntities.stream().sorted().findFirst().get();
        assertEquals("file1.vcf", file.getName());
        List<Reference> md5Refs = file.getAdditionalProperties()
                                      .stream()
                                      .filter(ref -> ref.getId().endsWith("md5"))
                                      .collect(Collectors.toList());
        List<RoCrateEntity> md5Entities = roCrateMetadata.getEntities(md5Refs);
        assertEquals(1, md5Entities.size());
        assertEquals("md5_1", ((CommentEntity) md5Entities.get(0)).getText());

        // Check sample entities
        List<SampleEntity> sampleEntities = roCrateMetadata.getEntities(analysis.getSamples()).stream().map(
                entity -> (SampleEntity) entity).collect(Collectors.toList());
        assertEquals(2, sampleEntities.size());
        assertEquals(Sets.newHashSet("SAMEA0001", "SAMEA0002"),
                     sampleEntities.stream().map(SampleEntity::getSampleAccession).collect(Collectors.toSet()));
        assertEquals(Sets.newHashSet("sample1_in_file1", "sample2_in_file1"),
                     sampleEntities.stream().map(SampleEntity::getName).collect(Collectors.toSet()));
    }

    @Test
    public void testGetRoCrateCatalog() {
        String url = "/v1/studies/ro-crate";
        ResponseEntity<RoCrateMetadata> response = restTemplate.exchange(url, HttpMethod.GET, null,
                RoCrateMetadata.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        RoCrateMetadata roCrateMetadata = response.getBody();

        // First entity describes the metadata document itself
        MetadataEntity metadata = (MetadataEntity) roCrateMetadata.getGraph().get(0);
        assertEquals("ro-crate-metadata.json", metadata.getId());

        // Second entity is the dataCatalog for all of EVA's projects
        DataCatalogEntity dataCatalog = (DataCatalogEntity) roCrateMetadata.getGraph().get(1);
        assertEquals("EVA studies", dataCatalog.getIdentifier());
        assertEquals(LocalDate.of(2025, 1, 1), dataCatalog.getDatePublished());

        // Check project entities (dataset)
        List<RoCrateEntity> projects = roCrateMetadata.getEntitiesOfType("Dataset");
        assertEquals(1, projects.size());
        DatasetMinimalProjectEntity project = (DatasetMinimalProjectEntity) projects.stream().sorted().findFirst().get();
        assertEquals("PRJEB0001", project.getProjectAccession());
    }

    @Test
    public void testGetRoCrateNotFound() {
        String url = "/v1/studies/ro-crate/PRJEB999";
        ResponseEntity<RoCrateMetadata> response = restTemplate.exchange(url, HttpMethod.GET, null,
                                                                         RoCrateMetadata.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
