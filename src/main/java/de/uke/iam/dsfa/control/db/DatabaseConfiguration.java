package de.uke.iam.dsfa.control.db;

import de.uke.iam.dsfa.control.Config;
import de.uke.iam.dsfa.control.config.Dsfa;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;


public class DatabaseConfiguration extends DefaultConfiguration {

    private static DatabaseConfiguration config;
    private final DSLContext dsl;

    private DatabaseConfiguration(DataSource ds) {
        set(ds);
        set(SQLDialect.POSTGRES);

        // Disable JOOQ logging
        Settings settings = new Settings();
        settings.withExecuteLogging(false);
        settings.withReturnAllOnUpdatableRecord(true);
        setSettings(settings);

        dsl = DSL.using(this);
    }

    public static DatabaseConfiguration get() {
        if (config == null) {
            Dsfa dsfaConfig = Config.instance.getConfig();
            String url = "jdbc:postgresql://" + dsfaConfig.getDatabaseConnection().getHost() + "/" + dsfaConfig.getDatabaseConnection().getDatabase();
            DataSource ds = new DataSource();
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setUsername(dsfaConfig.getDatabaseConnection().getUsername());
            ds.setPassword(dsfaConfig.getDatabaseConnection().getPassword());
            ds.setUrl(url);
            // configured like: https://serverfault.com/questions/885877/docker-swarm-database-connection-reset-by-peer
            ds.setInitialSize(5);
            ds.setMaxActive(10);
            ds.setMaxIdle(8);
            ds.setMinIdle(5);
            ds.setTestOnBorrow(true);
            ds.setTestWhileIdle(true);
            ds.setTestOnReturn(false);
            ds.setTestOnConnect(true);
            ds.setValidationQuery("SELECT 1");
            ds.setValidationInterval(30000);
            ds.setMaxWait(30000);
            ds.setMinEvictableIdleTimeMillis(60000);
            ds.setTimeBetweenEvictionRunsMillis(5000);
            ds.setRemoveAbandoned(true);
            ds.setRemoveAbandonedTimeout(60);
            ds.setDefaultAutoCommit(true);

            config = new DatabaseConfiguration(ds);
        }

        return config;
    }

    public DSLContext getDsl() {
        return dsl;
    }
}
