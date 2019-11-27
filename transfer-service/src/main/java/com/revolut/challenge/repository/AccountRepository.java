package com.revolut.challenge.repository;

import com.revolut.challenge.domain.Account;
import com.revolut.challenge.exception.AccountNotFoundException;

public interface AccountRepository {
    Account create(Account account);
    Account find(long id) throws AccountNotFoundException;
    Account update(Account account) throws AccountNotFoundException;
    void delete(long id) throws AccountNotFoundException;
}
