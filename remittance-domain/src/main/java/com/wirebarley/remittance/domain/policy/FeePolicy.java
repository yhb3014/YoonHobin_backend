package com.wirebarley.remittance.domain.policy;

public class FeePolicy {

    public long feeOf(long amount) {
        return amount / 100;
    }
}
