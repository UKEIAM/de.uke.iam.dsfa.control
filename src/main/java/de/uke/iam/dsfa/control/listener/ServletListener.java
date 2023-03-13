package de.uke.iam.dsfa.control.listener;

import de.uke.iam.dsfa.control.Config;
import de.uke.iam.dsfa.control.config.Dsfa;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.flywaydb.core.Flyway;

@WebListener
public class ServletListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    String fallback = servletContextEvent.getServletContext().getRealPath("/WEB-INF");
    Dsfa dsfaConfig = Config.instance.getConfig();

    String url = "jdbc:postgresql://" + dsfaConfig.getDatabaseConnection().getHost() + "/" + dsfaConfig.getDatabaseConnection().getDatabase();
    Flyway flyway = Flyway.configure()
        .dataSource(url, dsfaConfig.getDatabaseConnection().getUsername(),
            dsfaConfig.getDatabaseConnection().getPassword()).load();
    flyway.migrate();

  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {

  }
}
