package com.wirebarley.remittance.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wirebarley.remittance.domain.repository.TransactionRepository;
import com.wirebarley.remittance.domain.service.TransactionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionAppService {

    private final TransactionService transactionService;

    @Transactional(readOnly = true)
    public List<TransactionRepository.Tx> getTransactions(String accountNumber, int page, int size) {
        return transactionService.getTransactions(accountNumber, page, size);
    }
}
