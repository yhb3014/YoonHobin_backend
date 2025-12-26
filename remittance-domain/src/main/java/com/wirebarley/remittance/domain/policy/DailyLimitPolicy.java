package com.wirebarley.remittance.domain.policy;

import com.wirebarley.remittance.domain.transaction.TransactionType;

public class DailyLimitPolicy {

    public long limitOf(TransactionType type) {

        return switch (type) {
            case WITHDRAW -> 1000000L;
            case TRANSFER_OUT -> 3000000L;
            default -> Long.MAX_VALUE;
        };
    }
}
