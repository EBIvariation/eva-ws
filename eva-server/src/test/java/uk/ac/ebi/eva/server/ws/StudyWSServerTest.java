package uk.ac.ebi.eva.server.ws;

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
import uk.ac.ebi.eva.lib.entities.Platform;
import uk.ac.ebi.eva.lib.entities.Project;
import uk.ac.ebi.eva.lib.entities.Sample;
import uk.ac.ebi.eva.lib.entities.Submission;
import uk.ac.ebi.eva.lib.entities.Taxonomy;
import uk.ac.ebi.eva.lib.models.rocrate.CommentEntity;
import uk.ac.ebi.eva.lib.models.rocrate.DatasetEntity;
import uk.ac.ebi.eva.lib.models.rocrate.MetadataEntity;
import uk.ac.ebi.eva.lib.models.rocrate.Reference;
import uk.ac.ebi.eva.lib.models.rocrate.RoCrateEntity;
import uk.ac.ebi.eva.lib.models.rocrate.RoCrateMetadata;
import uk.ac.ebi.eva.lib.repositories.DbXrefRepository;
import uk.ac.ebi.eva.lib.repositories.ProjectRepository;
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

    @Before
    public void setUp() {
        Project project = new Project("PRJEB0001", "submitter center", "project alias", "project title", "description",
                                      "multi-isolate", "DNA", "other", "other", "studyId", "germline", 999L, null, null,
                                      null, null, "control set");
        Taxonomy taxonomy = new Taxonomy(9606L, "human", "Homo sapiens", "hsapiens", "human");
        DbXref dbXref = new DbXref(1L, "doi", "123", null, "publication", "project");
        Submission submission1 = new Submission(1L, "ERA123", "PROJECT", "ADD", "first submission", null,
                                                LocalDate.of(2025, 1, 1), 1);
        Submission submission2 = new Submission(2L, "ERA456", "PROJECT", "ADD", "second submission", null,
                                                LocalDate.of(2025, 7, 1), 1);
        List<Submission> submissions = Arrays.asList(submission1, submission2);

        Analysis analysis1 = new Analysis("ERZ0001", "analysis 1", "alias", "description", "center", null, "reference",
                                          "GCA_0001.1", 0, 1);
        Analysis analysis2 = new Analysis("ERZ0002", "analysis 2", "alias", "description", "center", null, "reference",
                                          "GCA_0001.2", 0, 1);
        List<Analysis> analyses = Arrays.asList(analysis1, analysis2);

        Platform platform = new Platform(1L, "Illumina HiSeq 2000", "Illumina");
        ExperimentType experimentType = new ExperimentType(1L, "Whole genome sequencing");
        File file1 = new File(1L, "ena file id", "file1.vcf", "md5checksum", "file location", "VCF", "submitted", 1,
                              true, null, false, "eva file id");
        File file2 = new File(2L, "ena file id", "file2.vcf", "md5checksum", "file location", "VCF", "submitted", 1,
                              true, null, false, "eva file id");
        Sample sample1 = new Sample(1L, "SAMEA0001", "ERS0001");
        Sample sample2 = new Sample(2L, "SAMEA0002", "ERS0002");
        FileSample fileSample11 = new FileSample(file1, sample1, "sample1_in_file1");
        FileSample fileSample12 = new FileSample(file1, sample2, "sample2_in_file1");
        FileSample fileSample22 = new FileSample(file2, sample2, "sample2_in_file2");

        project.setDbXrefs(Collections.singletonList(dbXref));
        project.setTaxonomies(Collections.singletonList(taxonomy));
        project.setSubmissions(submissions);
        project.setAnalyses(analyses);

        analysis1.setExperimentType(experimentType);
        analysis1.setPlatform(platform);
        analysis1.setSubmission(submission1);
        analysis1.setFiles(Collections.singletonList(file1));
        analysis2.setExperimentType(experimentType);
        analysis2.setPlatform(platform);
        analysis2.setSubmission(submission2);
        analysis2.setFiles(Collections.singletonList(file2));

        file1.setFileSamples(Arrays.asList(fileSample11, fileSample12));
        file2.setFileSamples(Collections.singletonList(fileSample22));

        // TODO repositories
        taxonomyRepository.save(taxonomy);
        dbXrefRepository.save(dbXref);
        submissionRepository.saveAll(submissions);
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
        DatasetEntity dataset = (DatasetEntity) roCrateMetadata.getGraph().get(1);
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

        // TODO add additional entities to test
    }

    @Test
    public void testGetRoCrateNotFound() {
        String url = "/v1/studies/ro-crate/PRJEB999";
        ResponseEntity<RoCrateMetadata> response = restTemplate.exchange(url, HttpMethod.GET, null,
                                                                         RoCrateMetadata.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
