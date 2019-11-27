package com.revolut.challenge.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.revolut.challenge.domain.Account;
import com.revolut.challenge.repository.AccountRepository;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AccountLockTest {

    private AccountService accountService;
    private ExecutorService executorService;

    @Before
    public void init() {
        AccountRepository repository = mock(AccountRepository.class);
        doReturn(Account.builder().id(1L).balance(new BigDecimal(10000000)).build()).when(repository).find(1L);
        doReturn(Account.builder().id(2L).balance(new BigDecimal(10000000)).build()).when(repository).find(2L);
        accountService = new AccountService(repository);
        executorService = Executors.newFixedThreadPool(2);
    }

    @Test(timeout = 5000)
    public void testLocks()  {
        List<Callable<Object>> tasks = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            BigDecimal amount = BigDecimal.valueOf((long) i);
            tasks.add(() -> { accountService.transfer(1L, 2L, amount); return null; });
            tasks.add(() -> { accountService.transfer(2L, 1L, amount); return null; });
        }
        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Account account1 = accountService.find(1L);
        Assert.assertEquals(new BigDecimal(10000000), account1.getBalance());

        Account account2 = accountService.find(2L);
        Assert.assertEquals(new BigDecimal(10000000), account2.getBalance());
    }
}
