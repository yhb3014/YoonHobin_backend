package com.wirebarley.remittance.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.wirebarley.remittance.domain.transaction.TransactionType;

public interface TransactionRepository {

    void save(Tx tx);

    long sumAmountByTypeBetween(String accountNumber, TransactionType type, Instant from, Instant to);

    List<Tx> findLatest(String accountNumber, int page, int size);

    record Tx(
            Long id,
            Long accountId,
            String accountNumber,
            TransactionType type,
            long amount,
            long feeAmount,
            Long counterpartyAccountId,
            String counterpartyAccountNumber,
            long balanceBefore,
            long balanceAfter,
            UUID groupId,
            Instant createdAt) {
    }
}
