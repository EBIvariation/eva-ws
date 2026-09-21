package uk.ac.ebi.eva.countstats.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.eva.countstats.model.Count;
import uk.ac.ebi.eva.countstats.repository.CountRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CountStatsIntegrationTest extends PostgresTestContainerHelper {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CountRepository countRepository;

    public static final String ADMIN_USERNAME = "username";
    public static final String ADMIN_PASSWORD = "password";

    @Test
    @Transactional
    public void testSaveCount() throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD);
        Count count1 = new Count("VARIANT_WAREHOUSE_INGESTION", "{\"study\": \"PRJ11111\", \"analysis\": \"ERZ11111\", \"batch\":1}",
                "INSERTED_VARIANTS", 10000L);
        Count count2 = new Count("VARIANT_WAREHOUSE_INGESTION", "{\"study\": \"PRJ11111\", \"analysis\": \"ERZ11111\", \"batch\":1}",
                "INSERTED_VARIANTS", 15000L);

        String response1 = mvc.perform(post("/v1/count")
                        .headers(httpHeaders)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(count1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String response2 = mvc.perform(post("/v1/count")
                        .headers(httpHeaders)
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