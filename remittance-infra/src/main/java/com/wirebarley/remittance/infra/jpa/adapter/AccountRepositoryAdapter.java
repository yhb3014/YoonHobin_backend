package com.wirebarley.remittance.infra.jpa.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.wirebarley.remittance.domain.repository.AccountRepository;
import com.wirebarley.remittance.infra.jpa.entity.AccountEntity;
import com.wirebarley.remittance.infra.jpa.repository.AccountJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAccountNumber(String accountNumber) {
        return accountJpaRepository.existsByAccountNumber(accountNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> find(String accountNumber) {
        return accountJpaRepository.findByAccountNumber(accountNumber).map(this::toDomain);
    }

    @Override
    public Optional<Account> findForUpdate(String accountNumber) {
        return accountJpaRepository.findByAccountNumberForUpdate(accountNumber).map(this::toDomain);
    }

    @Override
    public Account save(Account account) {
        if (account.id() == null) {
            AccountEntity saveEntity = accountJpaRepository
                    .save(new AccountEntity(account.accountNumber(), account.balance(), account.status()));
            return toDomain(saveEntity);
        }

        AccountEntity accountEntity = accountJpaRepository.findById(account.id())
                .orElseThrow(() -> new IllegalStateException("??? ????????"));
        accountEntity.update(account.balance(), account.status());

        return toDomain(accountEntity);
    }

    @Override
    public void deleteByAccountNumber(String accountNumber) {
        accountJpaRepository.deleteByAccountNumber(accountNumber);
    }

    private Account toDomain(AccountEntity e) {
        return new Account(e.getId(), e.getAccountNumber(), e.getBalance(), e.getStatus());
    }
}