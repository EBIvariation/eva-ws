package uk.ac.ebi.eva.countstats.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfiguration {
    /**
     * Advertise a relative server URL so Swagger UI calls the API using the scheme and host the page was loaded from.
     */
    @Bean
    public OpenAPI apiConfiguration(@Value("${server.servlet.context-path:/}") String contextPath) {
        return new OpenAPI()
                .servers(List.of(new Server().url(contextPath)));
    }
}
