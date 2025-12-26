package com.wirebarley.remittance.infra.jpa.adapter;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.wirebarley.remittance.domain.repository.TransactionRepository;
import com.wirebarley.remittance.domain.transaction.TransactionType;
import com.wirebarley.remittance.infra.jpa.entity.AccountEntity;
import com.wirebarley.remittance.infra.jpa.entity.TransactionEntity;
import com.wirebarley.remittance.infra.jpa.repository.AccountJpaRepository;
import com.wirebarley.remittance.infra.jpa.repository.TransactionJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository transactionJpaRepository;
    private final AccountJpaRepository accountJpaRepository;

    @Override
    public void save(Tx tx) {
        AccountEntity owner = accountJpaRepository.getReferenceById(tx.accountId());
        AccountEntity counterparty = (tx.counterpartyAccountId() == null)
                ? null
                : accountJpaRepository.getReferenceById(tx.counterpartyAccountId());

        TransactionEntity entity = new TransactionEntity(
                owner,
                counterparty,
                tx.type(),
                tx.amount(),
                tx.feeAmount(),
                tx.counterpartyAccountNumber(),
                tx.balanceBefore(),
                tx.balanceAfter(),
                tx.groupId());

        transactionJpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public long sumAmountByTypeBetween(String accountNumber, TransactionType type, Instant from, Instant to) {
        return transactionJpaRepository.sumAmountByTypeBetween(accountNumber, type, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tx> findLatest(String accountNumber, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);

        return transactionJpaRepository
                .findLatest(accountNumber, pageable)
                .map(this::toDomain)
                .toList();
    }

    private Tx toDomain(TransactionEntity e) {
        Long counterId = (e.getCounterpartyAccount() == null) ? null : e.getCounterpartyAccount().getId();
        String counterNumber = (e.getCounterpartyAccount() == null) ? e.getCounterpartyAccountNumber()
                : e.getCounterpartyAccount().getAccountNumber();

        return new Tx(
                e.getId(),
                e.getAccount().getId(),
                e.getAccount().getAccountNumber(),
                e.getType(),
                e.getAmount(),
                e.getFeeAmount(),
                counterId,
                counterNumber,
                e.getBalanceBefore(),
                e.getBalanceAfter(),
                e.getTxGroupId(),
                e.getCreatedAt());
    }
}
