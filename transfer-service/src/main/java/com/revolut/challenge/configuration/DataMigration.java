package com.revolut.challenge.configuration;

import java.util.Properties;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.internal.configuration.ConfigUtils;

public final class DataMigration {
    private DataMigration() {
        throw new AssertionError();
    }

    private static Flyway flyway;
    static {
        ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.configure(flywayProperties(PropertiesUtil.load("db/db.properties")));

        flyway = new Flyway(configuration);
    }

    public static void migrate() {
        // Because db's URL contains 'file' and H2 stores own stuff in file storage,
        // so if app terminates incorrectly, db's files won't be deleted
        flyway.clean();
        flyway.migrate();
    }

    public static void clean() {
        flyway.clean();
    }

    private static Properties flywayProperties(Properties properties) {
        Properties flywayProperties = new Properties();
        flywayProperties.setProperty(ConfigUtils.URL, properties.getProperty("db.url"));
        flywayProperties.setProperty(ConfigUtils.DRIVER, properties.getProperty("db.driver"));
        flywayProperties.setProperty(ConfigUtils.USER, properties.getProperty("db.user"));
        flywayProperties.setProperty(ConfigUtils.PASSWORD, properties.getProperty("db.password"));
        return flywayProperties;
    }
}
