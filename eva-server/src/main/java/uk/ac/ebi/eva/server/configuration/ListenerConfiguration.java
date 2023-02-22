package uk.ac.ebi.eva.server.configuration;

import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.eva.server.ws.CustomServletContextListener;

import javax.servlet.ServletContextListener;

@Configuration
public class ListenerConfiguration {
    @Bean
    public ServletListenerRegistrationBean<ServletContextListener> listenerRegistrationBean() {
        ServletListenerRegistrationBean<ServletContextListener> bean =
                new ServletListenerRegistrationBean<>();
        bean.setListener(new CustomServletContextListener());
        return bean;
    }
}
