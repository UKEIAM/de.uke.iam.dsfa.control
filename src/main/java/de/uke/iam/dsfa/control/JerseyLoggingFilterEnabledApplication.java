package de.uke.iam.dsfa.control;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class JerseyLoggingFilterEnabledApplication extends ResourceConfig {

  public JerseyLoggingFilterEnabledApplication() {
    register(
        new LoggingFeature(
            Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME),
            Level.INFO,
            LoggingFeature.Verbosity.PAYLOAD_ANY,
            10000));
  }
}
