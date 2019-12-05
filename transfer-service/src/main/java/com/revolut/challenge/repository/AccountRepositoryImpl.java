package com.revolut.challenge.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.sql.DataSource;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.revolut.challenge.domain.Account;
import com.revolut.challenge.exception.AccountNotFoundException;

import lombok.SneakyThrows;

@Singleton
public class AccountRepositoryImpl implements AccountRepository {

    private static final String UPDATE_ACCOUNT_SQL = "UPDATE account SET balance=? WHERE id=?";
    private final DataSource dataSource;

    @Inject
    public AccountRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @SneakyThrows
    @Override
    public Account create(Account account) {
        Long id = execute(
                "INSERT INTO account (balance) VALUES (?)",
                stmt -> {
                    try {
                        stmt.setBigDecimal(1, account.getBalance());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                stmt -> {
                    try {
                        ResultSet generatedKeys = stmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            return generatedKeys.getLong(1);
                        }

                        throw new SQLException("No row was inserted.");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                dataSource.getConnection()
        );

        return Account.builder().id(id).balance(account.getBalance()).build();
    }

    @SneakyThrows
    @Override
    public Account find(long id) {
        return execute(
                "SELECT id, balance FROM account WHERE id=?",
                stmt -> {
                    try {
                        stmt.setLong(1, id);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                },
                stmt -> {
                    try {
                        ResultSet result = stmt.getResultSet();
                        if (result.next()) {
                            return Account.builder()
                                    .id(result.getLong(1))
                                    .balance(result.getBigDecimal(2))
                                    .build();
                        }

                        throw new AccountNotFoundException(id);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                dataSource.getConnection()
        );
    }

    @SneakyThrows
    @Override
    public void delete(long id) {
        execute(
                "DELETE FROM account WHERE id=?",
                stmt -> {
                    try {
                        stmt.setLong(1, id);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                stmt -> {
                    try {
                        if (stmt.getUpdateCount() == 1) {
                            return id;
                        } else if (stmt.getUpdateCount() > 1) {
                            throw new SQLException("Several rows were deleted.");
                        }

                        throw new AccountNotFoundException(id);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                dataSource.getConnection()
        );
    }

    @SneakyThrows
    @Override
    public Account update(Account account) {
        return execute(
                UPDATE_ACCOUNT_SQL,
                stmt -> {
                    try {
                        stmt.setBigDecimal(1, account.getBalance());
                        stmt.setLong(2, account.getId());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, stmt -> {
                    try {
                        if (stmt.getUpdateCount() == 1) {
                            return account;
                        } else if (stmt.getUpdateCount() > 1) {
                            throw new SQLException("Several rows were updated.");
                        }

                        throw new AccountNotFoundException(account.getId());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                dataSource.getConnection()
        );
    }

    @SneakyThrows
    public void updateTransactional(List<Account> toUpdate) throws AccountNotFoundException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            for (Account account : toUpdate) {
                try (PreparedStatement updateStmt = connection.prepareStatement(UPDATE_ACCOUNT_SQL)) {
                    long id = account.getId();

                    updateStmt.setBigDecimal(1, account.getBalance());
                    updateStmt.setLong(2, id);
                    updateStmt.executeUpdate();

                    if (updateStmt.getUpdateCount() != 1) {
                        throw new AccountNotFoundException(id);
                    } else if (updateStmt.getUpdateCount() > 1) {
                        throw new SQLException("Several rows were deleted.");
                    }
                }
            }

            connection.commit();
        } catch (Exception e ) {
            if (connection != null) {
                connection.rollback();
                throw e;
            }
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
            }
        }
    }

    @SneakyThrows
    private <T> T execute(String sqlQuery,
                          Consumer<PreparedStatement> setParams,
                          Function<PreparedStatement, T> getResult,
                          Connection connection)
    {
        try {
            connection.setAutoCommit(false);

            PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            setParams.accept(preparedStmt);

            preparedStmt.execute();

            connection.commit();
            return getResult.apply(preparedStmt);
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
