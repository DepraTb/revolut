package com.revolut.challenge.service;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.revolut.challenge.configuration.AppConfig;
import com.revolut.challenge.configuration.DataMigration;
import com.revolut.challenge.domain.Account;
import com.revolut.challenge.exception.AccountNotFoundException;
import com.revolut.challenge.exception.InvalidTransferException;

public class AccountServiceTest {

    private static final Injector INJECTOR = Guice.createInjector(new AppConfig());

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private AccountService accountService;

    @Before
    public void init() {
        DataMigration.migrate();
        accountService = INJECTOR.getInstance(AccountService.class);
    }

    @After
    public void clean() {
        DataMigration.clean();
    }

    @Test
    public void testCreate() {
        Account account = accountService.create(new BigDecimal(2000));

        Assert.assertEquals(7L, account.getId());
        Assert.assertEquals(new BigDecimal(2000), account.getBalance());
    }

    @Test
    public void testCreate_expectedIllegalArgumentException_balanceIsNegative() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("The balance value must not be negative.");

        accountService.create(new BigDecimal(-2000));
    }

    @Test
    public void testFind_expectedAccountNotFoundException(){
        long toFind = 8L;

        thrown.expect(AccountNotFoundException.class);
        thrown.expectMessage("Account with id '" + toFind + "' was not found.");

        accountService.find(toFind);
    }

    @Test
    public void testDelete_expectedAccountNotFoundException(){
        long toDelete = 8L;

        thrown.expect(AccountNotFoundException.class);
        thrown.expectMessage("Account with id '" + toDelete + "' was not found.");

        accountService.delete(toDelete);
    }

    @Test
    public void testTransfer() {
        accountService.transfer(1L, 2L, new BigDecimal(2000));
    }

    @Test
    public void testTransfer_expectedAccountNotFoundException_fromAccountWasNotFound() {
        long from = 8L;

        thrown.expect(AccountNotFoundException.class);
        thrown.expectMessage("Account with id '" + from + "' was not found.");

        accountService.transfer(from, 2L, new BigDecimal(2000));
    }

    @Test
    public void testTransfer_expectedAccountNotFoundException_toAccountWasNotFound() {
        long from = 1L;
        long to = 8L;

        thrown.expect(AccountNotFoundException.class);
        thrown.expectMessage("Account with id '" + to + "' was not found.");

        accountService.transfer(from, to, new BigDecimal(2000));
    }

    @Test
    public void testTransfer_expectedInvalidTransferException_transferBetweenOneAccount() {
        thrown.expect(InvalidTransferException.class);
        thrown.expectMessage("The transfer of money to the same account is not allowed.");

        accountService.transfer(1L, 1L, new BigDecimal(2001));
    }

    @Test
    public void testTransfer_expectedInvalidTransferException_fromAccountHasNotEnoughMoney() {
        thrown.expect(InvalidTransferException.class);
        thrown.expectMessage("Not enough money on balance.");

        accountService.transfer(1L, 2L, new BigDecimal(2001));
    }
}
