package uk.ac.ebi.eva.server.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfiguration {
    /**
     * Advertise a relative server URL so Swagger UI calls the API using the scheme and host the page was loaded from.
     **/
    @Bean
    public OpenAPI apiConfiguration(@Value("${server.servlet.context-path:/}") String contextPath) {
        return new OpenAPI()
                .servers(List.of(new Server().url(contextPath)))
                .info(new Info()
                        .title("European Variation Archive REST Web Services API")
                        .version("1.0")
                        .contact(new Contact()
                                .name("the European Variation Archive team")
                                .url("www.ebi.ac.uk/eva")
                                .email("eva-helpdesk@ebi.ac.uk"))
                        .license(new License()
                                .name("Apache License Version 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }

}
