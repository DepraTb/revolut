package com.revolut.challenge.configuration;

import java.util.Properties;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

import com.google.inject.AbstractModule;
import com.revolut.challenge.controller.AccountController;
import com.revolut.challenge.repository.AccountRepository;
import com.revolut.challenge.repository.AccountRepositoryImpl;
import com.revolut.challenge.service.AccountService;

import lombok.SneakyThrows;

public class AppConfig extends AbstractModule {

    @SneakyThrows
    @Override
    protected void configure() {
        bind(DataSource.class).toInstance(h2DataSource());
        bind(AccountRepositoryImpl.class).toConstructor(AccountRepositoryImpl.class.getConstructor(DataSource.class));
        bind(AccountRepository.class).to(AccountRepositoryImpl.class);
        bind(AccountService.class).toConstructor(AccountService.class.getConstructor(AccountRepository.class));
        bind(AccountController.class).toConstructor(AccountController.class.getConstructor(AccountService.class));
    }

    private static DataSource h2DataSource() {
        Properties properties = PropertiesUtil.load("db/db.properties");

        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl(properties.getProperty("db.url"));
        dataSource.setUser(properties.getProperty("db.user"));
        dataSource.setPassword(properties.getProperty("db.password"));

        return dataSource;
    }
}
