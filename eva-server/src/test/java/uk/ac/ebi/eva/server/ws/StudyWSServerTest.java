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

import uk.ac.ebi.eva.lib.entities.DbXref;
import uk.ac.ebi.eva.lib.entities.Project;
import uk.ac.ebi.eva.lib.entities.Taxonomy;
import uk.ac.ebi.eva.lib.models.rocrate.CommentEntity;
import uk.ac.ebi.eva.lib.models.rocrate.DatasetEntity;
import uk.ac.ebi.eva.lib.models.rocrate.MetadataEntity;
import uk.ac.ebi.eva.lib.models.rocrate.Reference;
import uk.ac.ebi.eva.lib.models.rocrate.RoCrateEntity;
import uk.ac.ebi.eva.lib.models.rocrate.RoCrateMetadata;
import uk.ac.ebi.eva.lib.repositories.DbXrefRepository;
import uk.ac.ebi.eva.lib.repositories.ProjectRepository;
import uk.ac.ebi.eva.lib.repositories.TaxonomyRepository;

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

    @Before
    public void setUp() {
        Project project = new Project("PRJEB0001", "submitter center", "project alias", "project title", "description",
                                      "multi-isolate", "DNA", "other", "other", "studyId", "germline", 999L, null, null,
                                      null, null, "control set");
        Taxonomy taxonomy = new Taxonomy(9606L, "human", "Homo sapiens", "hsapiens", "human");
        DbXref dbXref = new DbXref(1L, "doi", "123", null, "publication", "project");
        project.setDbXrefs(Collections.singletonList(dbXref));
        project.setTaxonomies(Collections.singletonList(taxonomy));
        taxonomyRepository.save(taxonomy);
        dbXrefRepository.save(dbXref);
        projectRepository.save(project);
    }

    @Test
    public void testGetRoCrate() {
        String url = "/v1/studies/PRJEB0001/ro-crate";
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
        List<Reference> taxonomyRefs = dataset.getAdditionalProperties()
                                              .stream()
                                              .filter(ref -> ref.getId().equalsIgnoreCase("#taxonomyId"))
                                              .collect(Collectors.toList());
        List<RoCrateEntity> taxonomyEntities = roCrateMetadata.getEntities(taxonomyRefs);
        assertEquals(1, taxonomyEntities.size());
        assertEquals("9606", ((CommentEntity) taxonomyEntities.get(0)).getText());
    }
}
