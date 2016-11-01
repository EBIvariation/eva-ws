package uk.ac.ebi.eva.server.security.authorization;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import uk.ac.ebi.eva.server.Profiles;


@Configuration
@Profile(Profiles.OAUTH_SECURITY)
@EnableResourceServer
public class Oauth2Configuration extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.anonymous() // Enable anonymous / configure any related anonymous role
                .and()
                .authorizeRequests().antMatchers("/webservices/rest/swagger-ui.html**", "/webservices/rest/swagger-resources/**", "/webservices/rest/webjars/springfox-swagger-ui/**", "/webservices/rest/webservices/api").permitAll() //Authorize /hearbeat for everybody
                .antMatchers("/**").authenticated() // The rest need to be authenticated
        ;
    }

}
