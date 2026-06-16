package uk.ac.ebi.eva.countstats.integration;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static uk.ac.ebi.eva.countstats.integration.CountStatsIntegrationTest.ADMIN_PASSWORD;
import static uk.ac.ebi.eva.countstats.integration.CountStatsIntegrationTest.ADMIN_USERNAME;

@Testcontainers
public abstract class PostgresTestContainerHelper {

    private static final String POSTGRES_IMAGE = "postgres:11";

    @Container
    @ServiceConnection
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer(POSTGRES_IMAGE);

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("controller.auth.admin.username", () -> ADMIN_USERNAME);
        registry.add("controller.auth.admin.password", () -> ADMIN_PASSWORD);
    }
}