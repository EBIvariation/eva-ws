package uk.ac.ebi.eva.server.ws;

import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.support.WebApplicationContextUtils;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class CustomServletContextListener
        implements ServletContextListener {

    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        System.out.println("Callback triggered - ContextListener.");
        try {
            MultiMongoDbFactory.unset();
            this.threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
            this.threadPoolTaskExecutor.shutdown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
        this.threadPoolTaskExecutor = (ThreadPoolTaskExecutor) (applicationContext.getBean(ThreadPoolTaskExecutor.class));
    }
}
