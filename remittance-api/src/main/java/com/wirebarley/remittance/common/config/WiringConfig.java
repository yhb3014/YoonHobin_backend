package com.wirebarley.remittance.common.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wirebarley.remittance.domain.policy.DailyLimitPolicy;
import com.wirebarley.remittance.domain.policy.FeePolicy;
import com.wirebarley.remittance.domain.repository.AccountRepository;
import com.wirebarley.remittance.domain.repository.TransactionRepository;
import com.wirebarley.remittance.domain.service.AccountService;
import com.wirebarley.remittance.domain.service.TransactionService;
import com.wirebarley.remittance.domain.service.TransferService;

@Configuration
public class WiringConfig {

    @Bean
    public FeePolicy feePolicy() {
        return new FeePolicy();
    }

    @Bean
    public DailyLimitPolicy dailyLimitPolicy() {
        return new DailyLimitPolicy();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public AccountService accountService(AccountRepository accountRepository) {
        return new AccountService(accountRepository);
    }

    @Bean
    public TransactionService transactionService(TransactionRepository transactionRepository) {
        return new TransactionService(transactionRepository);
    }

    @Bean
    public TransferService transferService(AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            FeePolicy feePolicy,
            DailyLimitPolicy dailyLimitPolicy,
            Clock clock) {
        return new TransferService(accountRepository, transactionRepository, clock, dailyLimitPolicy, feePolicy);
    }
}
