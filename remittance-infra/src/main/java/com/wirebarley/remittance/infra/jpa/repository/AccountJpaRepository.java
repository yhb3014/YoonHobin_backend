package com.wirebarley.remittance.infra.jpa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wirebarley.remittance.infra.jpa.entity.AccountEntity;

import jakarta.persistence.LockModeType;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {

    boolean existsByAccountNumber(String accountNumber);

    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    void deleteByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccountEntity a where a.accountNumber = :accountNumber")
    Optional<AccountEntity> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);
}