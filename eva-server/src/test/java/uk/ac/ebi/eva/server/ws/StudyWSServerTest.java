package uk.ac.ebi.eva.server.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
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
import uk.ac.ebi.eva.lib.models.rocrate.Comment;
import uk.ac.ebi.eva.lib.models.rocrate.Dataset;
import uk.ac.ebi.eva.lib.repositories.DbXrefRepository;
import uk.ac.ebi.eva.lib.repositories.ProjectRepository;
import uk.ac.ebi.eva.lib.repositories.TaxonomyRepository;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    private ObjectMapper objectMapper;

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
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Configuration configuration = Configuration.defaultConfiguration()
                                                   .jsonProvider(new JacksonJsonProvider())
                                                   .mappingProvider(new JacksonMappingProvider(objectMapper))
                                                   .addOptions(Option.SUPPRESS_EXCEPTIONS);
        Dataset dataset = JsonPath.using(configuration).parse(response.getBody())
                                                        .read("$['@graph'][0]", Dataset.class);

        assertEquals("PRJEB0001", dataset.getProjectAccession());
        assertEquals("project title", dataset.getName());
        Set<String> taxonomies = dataset.getAdditionalProperties()
                                        .stream()
                                        .filter(comment -> comment.getName().equalsIgnoreCase("taxonomyId"))
                                        .map(Comment::getText)
                                        .collect(Collectors.toSet());
        assertEquals(1, taxonomies.size());
        assertTrue(taxonomies.contains("9606"));
    }
}
