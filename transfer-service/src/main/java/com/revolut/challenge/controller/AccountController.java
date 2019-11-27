package com.revolut.challenge.controller;

import java.math.BigDecimal;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jetty.http.HttpStatus;

import com.revolut.challenge.service.AccountService;

import io.javalin.http.Context;

@Singleton
public class AccountController {

    private final AccountService service;

    @Inject
    public AccountController(AccountService service) {
        this.service = service;
    }

    public void find(Context ctx) throws Exception {
        Long id = ctx.pathParam("id", Long.class).get();

        ctx.json(service.find(id));
        ctx.status(HttpStatus.OK_200);
    }

    public void delete(Context ctx) {
        Long id = ctx.pathParam("id", Long.class).get();

        service.delete(id);
        ctx.status(HttpStatus.NO_CONTENT_204);
    }

    public void create(Context ctx) {
        BigDecimal balance = ctx.pathParam("balance", BigDecimal.class).get();

        ctx.json(service.create(balance));
        ctx.status(HttpStatus.CREATED_201);
    }

    public void transfer(Context ctx) {
        Long fromId = ctx.pathParam("fromId", Long.class).get();
        Long toId = ctx.pathParam("toId", Long.class).get();
        BigDecimal amount = ctx.pathParam("amount", BigDecimal.class).get();

        service.transfer(fromId, toId, amount);
        ctx.status(HttpStatus.NO_CONTENT_204);
    }
}
