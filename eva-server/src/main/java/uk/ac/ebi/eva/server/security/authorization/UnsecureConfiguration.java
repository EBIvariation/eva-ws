package uk.ac.ebi.eva.server.security.authorization;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@ConditionalOnProperty(prefix = "eva.server.security", name = "enabled", matchIfMissing = true, havingValue = "false")
@Configuration
@EnableResourceServer
public class UnsecureConfiguration extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.anonymous() // Enable anonymous / configure any related anonymous role
                .and()
                .authorizeRequests().antMatchers("/**").permitAll() //Authorize anonymous access to every mapping.
        ;
    }

}