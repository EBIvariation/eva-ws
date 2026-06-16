package uk.ac.ebi.eva.server.security.authorization;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import uk.ac.ebi.eva.server.Profiles;

@Configuration
@Profile(Profiles.NOT_OAUTHSECURITY)
public class UnsecureConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.anonymous(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}