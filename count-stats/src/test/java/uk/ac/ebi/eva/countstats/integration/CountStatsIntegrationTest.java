package uk.ac.ebi.eva.countstats.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.ac.ebi.eva.countstats.model.Count;
import uk.ac.ebi.eva.countstats.repository.CountRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ContextConfiguration(initializers = CountStatsIntegrationTest.DockerPostgreDataSourceInitializer.class)
public class CountStatsIntegrationTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CountRepository countRepository;

    @Container
    public static PostgreSQLContainer<?> postgreDBContainer = new PostgreSQLContainer<>("postgres:9.6");

    public static class DockerPostgreDataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + postgreDBContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreDBContainer.getUsername(),
                    "spring.datasource.password=" + postgreDBContainer.getPassword()
            );
        }
    }

    @Test
    @Transactional
    public void testSaveCount() throws Exception {
        Count count1 = new Count("VARIANT_WAREHOUSE_INGESTION", "{\"study\": \"PRJ11111\", \"analysis\": \"ERZ11111\", \"batch\":1}",
                "INSERTED_VARIANTS", 10000);
        Count count2 = new Count("VARIANT_WAREHOUSE_INGESTION", "{\"study\": \"PRJ11111\", \"analysis\": \"ERZ11111\", \"batch\":1}",
                "INSERTED_VARIANTS", 15000);

        String response1 = mvc.perform(post("/v1/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(count1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String response2 = mvc.perform(post("/v1/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(count2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long id1 = objectMapper.readTree(response1).get("id").longValue();
        Optional<Count> resCount1 = countRepository.findById(id1);
        assertThat(resCount1.get()).isNotNull();
        assertThat(resCount1.get().getCount()).isEqualTo(10000);

        long id2 = objectMapper.readTree(response2).get("id").longValue();
        Optional<Count> resCount2 = countRepository.findById(id2);
        assertThat(resCount2.get()).isNotNull();
        assertThat(resCount2.get().getCount()).isEqualTo(15000);

        Long totalCount = countRepository.getCountForProcess("VARIANT_WAREHOUSE_INGESTION", "PRJ11111");
        assertThat(totalCount).isEqualTo(25000);
    }

}