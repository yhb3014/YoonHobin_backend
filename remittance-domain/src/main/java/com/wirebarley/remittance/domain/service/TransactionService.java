package com.wirebarley.remittance.domain.service;

import java.time.Instant;
import java.util.List;

import com.wirebarley.remittance.domain.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * 거래내역 조회
     * 
     * @param accountNumber
     * @param page
     * @param size
     * @return 거래내역
     */
    public List<TransactionRepository.Tx> getTransactions(String accountNumber, int page, int size) {
        return transactionRepository.findLatest(accountNumber, page, size);
    }
}
