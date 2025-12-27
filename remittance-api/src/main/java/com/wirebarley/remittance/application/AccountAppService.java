package com.wirebarley.remittance.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wirebarley.remittance.domain.service.AccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountAppService {

    private final AccountService accountService;

    @Transactional
    public void register(String accountNumber) {
        accountService.register(accountNumber);
    }

    @Transactional
    public void delete(String accountNumber) {
        accountService.delete(accountNumber);
    }

    @Transactional(readOnly = true)
    public long getBalance(String accountNumber) {
        return accountService.getBalance(accountNumber);
    }
}
