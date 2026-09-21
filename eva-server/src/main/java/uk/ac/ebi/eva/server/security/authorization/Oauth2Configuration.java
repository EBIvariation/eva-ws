package uk.ac.ebi.eva.server.security.authorization;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import uk.ac.ebi.eva.server.Profiles;


@Configuration
@Profile(Profiles.OAUTH_SECURITY)
public class Oauth2Configuration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.anonymous(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/webservices/rest/swagger-ui.html",
                                "/webservices/rest/swagger-ui/**",
                                "/webservices/rest/v3/api-docs/**",
                                "/webservices/rest/webjars/**"
                        ).permitAll().anyRequest().authenticated()
                ).oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(Customizer.withDefaults())
                        // TODO: change based on the type of the token (jwt/opaque)
                );

        return http.build();
    }
}
