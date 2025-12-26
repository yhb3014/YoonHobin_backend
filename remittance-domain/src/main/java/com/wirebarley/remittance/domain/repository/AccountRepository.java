package com.wirebarley.remittance.domain.repository;

import java.util.Optional;

import com.wirebarley.remittance.domain.account.AccountStatus;

public interface AccountRepository {

    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> find(String accountNumber);

    Optional<Account> findForUpdate(String accountNumber);

    Account save(Account account);

    void deleteByAccountNumber(String accountNumber);

    record Account(
            Long id,
            String accountNumber,
            long balance,
            AccountStatus status) {
    }
}