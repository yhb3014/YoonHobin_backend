package com.wirebarley.remittance.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wirebarley.remittance.domain.repository.TransactionRepository;
import com.wirebarley.remittance.domain.transaction.TransactionType;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

        @Mock
        private TransactionRepository transactionRepository;

        @InjectMocks
        private TransactionService transactionService;

        private static final String ACCOUNT_NUMBER = "111-111-111";

        @Test
        void 거래내역조회() {
                Instant createdAt = Instant.parse("2025-01-01T12:34:56Z");

                TransactionRepository.Tx tx = new TransactionRepository.Tx(
                                1L, 2L, ACCOUNT_NUMBER, TransactionType.DEPOSIT, 10000L, 0L, null, null, 30000L, 40000L,
                                null,
                                createdAt);

                List<TransactionRepository.Tx> list = List.of(tx);
                when(transactionRepository.findLatest(ACCOUNT_NUMBER, 1, 20))
                                .thenReturn(list);

                List<TransactionRepository.Tx> result = transactionService.getTransactions(ACCOUNT_NUMBER, 1, 20);
                assertThat(list).isSameAs(result);
        }
}