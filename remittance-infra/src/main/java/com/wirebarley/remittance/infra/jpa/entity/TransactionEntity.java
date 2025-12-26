package com.wirebarley.remittance.infra.jpa.entity;

import java.time.Instant;
import java.util.UUID;

import com.wirebarley.remittance.domain.transaction.TransactionType;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_account_created_at", columnList = "account_id, created_at"),
        @Index(name = "idx_tx_account_type_created_at", columnList = "account_id, type, created_at"),
        @Index(name = "idx_tx_group_id", columnList = "tx_group_id")
})
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private TransactionType type;

    @Column(name = "amount", nullable = false)
    private long amount;

    @Column(name = "fee_amount", nullable = false)
    private long feeAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_account_id")
    private AccountEntity counterpartyAccount;

    @Column(name = "counterparty_account_number", length = 50)
    private String counterpartyAccountNumber;

    @Column(name = "balance_before", nullable = false)
    private long balanceBefore;

    @Column(name = "balance_after", nullable = false)
    private long balanceAfter;

    @Column(name = "tx_group_id")
    private UUID txGroupId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public TransactionEntity(
            AccountEntity account,
            AccountEntity counterpartyAccount,
            TransactionType type,
            long amount,
            long feeAmount,
            String counterpartyAccountNumber,
            long balanceBefore,
            long balanceAfter,
            UUID txGroupId) {
        this.account = account;
        this.counterpartyAccount = counterpartyAccount;
        this.type = type;
        this.amount = amount;
        this.feeAmount = feeAmount;
        this.counterpartyAccountNumber = counterpartyAccountNumber;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.txGroupId = txGroupId;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public static TransactionEntity of(
            AccountEntity account,
            AccountEntity counterpartyAccount,
            TransactionType type,
            long amount,
            long feeAmount,
            String counterpartyAccountNumber,
            long balanceBefore,
            long balanceAfter,
            UUID txGroupId) {
        return new TransactionEntity(account, counterpartyAccount, type, amount, feeAmount, counterpartyAccountNumber,
                balanceBefore, balanceAfter, txGroupId);
    }
}
