package com.wirebarley.remittance.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wirebarley.remittance.domain.service.TransferService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferAppService {

    private final TransferService transferService;

    @Transactional
    public long deposit(String accountName, long amount) {
        return transferService.deposit(accountName, amount);
    }

    @Transactional
    public long withdraw(String accountNumber, long amount) {
        return transferService.withdraw(accountNumber, amount);
    }

    @Transactional
    public TransferService.TransferResult transfer(String from, String to, long amount) {
        return transferService.transfer(from, to, amount);
    }
}
