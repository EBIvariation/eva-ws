package uk.ac.ebi.eva.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration
@EnableResourceServer
public class AuthorizationConfiguration extends ResourceServerConfigurerAdapter{

    public void configure(HttpSecurity http) throws Exception {
        http.anonymous() // Enable anonymous / configure any related anonymous role
                .and()
                .authorizeRequests().antMatchers("/heartbeat").permitAll() //Authorize /hearbeat for everybody
                .antMatchers("/**").authenticated() // The rest need to be authenticated
        ;
    }

}
