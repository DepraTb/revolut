package com.revolut.challenge;

import java.math.BigDecimal;

import org.eclipse.jetty.http.HttpStatus;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.revolut.challenge.configuration.AppConfig;
import com.revolut.challenge.configuration.DataMigration;
import com.revolut.challenge.controller.AccountController;
import com.revolut.challenge.exception.AccountNotFoundException;
import com.revolut.challenge.exception.InvalidTransferException;

import io.javalin.Javalin;
import io.javalin.core.event.EventListener;
import io.javalin.core.validation.JavalinValidation;
import kotlin.jvm.functions.Function1;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class App {
    private static final Injector INJECTOR = Guice.createInjector(new AppConfig());
    private Javalin javalin = Javalin.create();

    public static void main(String[] args) {
        new App().start();
    }

    void start() {
        DataMigration.migrate();
        JavalinValidation.register(BigDecimal.class, (Function1<String, BigDecimal>) BigDecimal::new);

        configureMapping();
        configureExceptionMapping();

        javalin.events((EventListener event) -> event.serverStopped(DataMigration::clean));
        javalin.start(Integer.parseInt(System.getProperty("http.port", "8080")));
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    void stop() {
        javalin.stop();
    }

    private void configureMapping() {
        AccountController accountController = INJECTOR.getInstance(AccountController.class);
        javalin.routes(() -> {
            path("account", () -> {
                path(":balance", () -> post(accountController::create));
                path(":id", () -> {
                    get(accountController::find);
                    delete(accountController::delete);
                });
                path(":fromId/transfer/:toId/:amount", () -> post(accountController::transfer));
            });
        });
    }

    private void configureExceptionMapping() {
        javalin.exception(AccountNotFoundException.class, (e, ctx) -> {
            ctx.result(e.getMessage());
            ctx.status(HttpStatus.NOT_FOUND_404);
        });
        javalin.exception(InvalidTransferException.class, (e, ctx) -> {
            ctx.result(e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST_400);
        });
        javalin.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.result(e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST_400);
        });
    }
}
