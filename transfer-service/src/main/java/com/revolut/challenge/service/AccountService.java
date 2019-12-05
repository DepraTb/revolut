package com.revolut.challenge.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.revolut.challenge.repository.AccountRepository;
import com.revolut.challenge.domain.Account;
import com.revolut.challenge.exception.InvalidTransferException;

@Singleton
public class AccountService {
    private final Map<Long, Lock> accountLocks = new ConcurrentHashMap<>();

    private final AccountRepository accountRepository;

    @Inject
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account find(long id) {
        return accountRepository.find(id);
    }

    public void delete(long id) {
        accountRepository.delete(id);
    }

    public Account create(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("The balance value must not be negative.");
        }
        return accountRepository.create(Account.builder().balance(balance).build());
    }

    public void transfer(long fromId, long toId, BigDecimal amount) {
        if (Objects.equals(fromId, toId)) {
            throw new InvalidTransferException("The transfer of money to the same account is not allowed.");
        }

        Lock lockFrom = accountLocks.computeIfAbsent(fromId, ignored -> new ReentrantLock());
        Lock lockTo = accountLocks.computeIfAbsent(toId, ignored -> new ReentrantLock());

        boolean gotAllLocks = false;
        do {
            if (lockFrom.tryLock()) {
                if (lockTo.tryLock()) {
                    gotAllLocks = true;
                } else {
                    lockFrom.unlock();
                }
            }
        } while (!gotAllLocks);

        Account from = accountRepository.find(fromId);
        if (from.getBalance().compareTo(amount) < 0) {
            throw new InvalidTransferException("Not enough money on balance.");
        }

        Account to = accountRepository.find(toId);

        try {
            from.setBalance(from.getBalance().subtract(amount));
            to.setBalance(to.getBalance().add(amount));

            accountRepository.updateTransactional(Lists.newArrayList(from, to));
        } finally {
            lockFrom.unlock();
            lockTo.unlock();
        }
    }
}
