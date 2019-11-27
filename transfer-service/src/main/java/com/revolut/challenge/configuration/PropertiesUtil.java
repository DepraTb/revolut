package com.revolut.challenge.configuration;

import java.io.InputStream;
import java.util.Properties;

import lombok.SneakyThrows;

final class PropertiesUtil {
    private PropertiesUtil() {
        throw new AssertionError();
    }

    @SneakyThrows
    static Properties load(String fn) {
        try (InputStream propsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fn)) {
            Properties properties = new Properties();
            properties.load(propsStream);
            return properties;
        }
    }
}
