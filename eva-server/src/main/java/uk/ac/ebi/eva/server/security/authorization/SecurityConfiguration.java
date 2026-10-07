package uk.ac.ebi.eva.server.security.authorization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security only protects the actuator endpoints. The API itself is open: every request to it is permitted.
 */
@Configuration
public class SecurityConfiguration {

    private static final String ROLE_ACTUATOR_ADMIN = "ACTUATOR_ADMIN";

    @Value("${actuator.auth.username}")
    private String USERNAME_ACTUATOR;

    @Value("${actuator.auth.password}")
    private String PASSWORD_ACTUATOR;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager actuatorUserDetailsManager() {
        return new InMemoryUserDetailsManager(
                User.withUsername(USERNAME_ACTUATOR)
                        .password(passwordEncoder().encode(PASSWORD_ACTUATOR))
                        .roles(ROLE_ACTUATOR_ADMIN)
                        .build()
        );
    }

    /**
     * Every actuator endpoint (served on the management port) requires the actuator user.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(EndpointRequest.toAnyEndpoint())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole(ROLE_ACTUATOR_ADMIN))
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
