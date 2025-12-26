package com.wirebarley.remittance.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wirebarley.remittance.domain.account.AccountStatus;
import com.wirebarley.remittance.domain.policy.DailyLimitPolicy;
import com.wirebarley.remittance.domain.policy.FeePolicy;
import com.wirebarley.remittance.domain.repository.AccountRepository;
import com.wirebarley.remittance.domain.repository.TransactionRepository;
import com.wirebarley.remittance.domain.transaction.TransactionType;
import com.wirebarley.remittance.exception.BusinessException;
import com.wirebarley.remittance.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private TransactionRepository transactionRepository;

        @Mock
        private DailyLimitPolicy dailyLimitPolicy;

        @Mock
        private FeePolicy feePolicy;

        private TransferService transferService;

        private static final String ACCOUNT_NUMBER = "111-111-111";
        private static final String COUNTER_ACCOUNT_NUMBER = "111-111-112";

        @BeforeEach
        void setUp() {
                Instant now = Instant.parse("2025-01-01T00:00:00Z");
                Clock clock = Clock.fixed(now, ZoneOffset.UTC);
                transferService = new TransferService(accountRepository, transactionRepository, clock,
                                new DailyLimitPolicy(), new FeePolicy());
        }

        @Test
        void 입금() {
                AccountRepository.Account account = new AccountRepository.Account(
                                1L, ACCOUNT_NUMBER, 1000L, AccountStatus.ACTIVE);
                given(accountRepository.findForUpdate(ACCOUNT_NUMBER)).willReturn(Optional.of(account));

                long balance = transferService.deposit(ACCOUNT_NUMBER, 500L);

                assertThat(balance).isEqualTo(1500L);

                ArgumentCaptor<AccountRepository.Account> accountCaptor = ArgumentCaptor
                                .forClass(AccountRepository.Account.class);
                verify(accountRepository).save(accountCaptor.capture());

                assertThat(accountCaptor.getValue().balance()).isEqualTo(1500L);

                ArgumentCaptor<TransactionRepository.Tx> txCaptor = ArgumentCaptor
                                .forClass(TransactionRepository.Tx.class);
                verify(transactionRepository).save(txCaptor.capture());

                assertThat(txCaptor.getValue().type()).isEqualTo(TransactionType.DEPOSIT);
                assertThat(txCaptor.getValue().amount()).isEqualTo(500L);
                assertThat(txCaptor.getValue().balanceBefore()).isEqualTo(1000L);
                assertThat(txCaptor.getValue().balanceAfter()).isEqualTo(1500L);
        }

        @Test
        void 출금() {
                AccountRepository.Account account = new AccountRepository.Account(
                                1L, ACCOUNT_NUMBER, 1000L, AccountStatus.ACTIVE);
                given(accountRepository.findForUpdate(ACCOUNT_NUMBER)).willReturn(Optional.of(account));

                given(transactionRepository.sumAmountByTypeBetween(
                                eq(ACCOUNT_NUMBER), eq(TransactionType.WITHDRAW), any(Instant.class),
                                any(Instant.class)))
                                .willReturn(0L);

                long balance = transferService.withdraw(ACCOUNT_NUMBER, 500L);

                assertThat(balance).isEqualTo(500L);

                ArgumentCaptor<AccountRepository.Account> accountCaptor = ArgumentCaptor
                                .forClass(AccountRepository.Account.class);
                verify(accountRepository).save(accountCaptor.capture());

                assertThat(accountCaptor.getValue().balance()).isEqualTo(500L);

                ArgumentCaptor<TransactionRepository.Tx> txCaptor = ArgumentCaptor
                                .forClass(TransactionRepository.Tx.class);
                verify(transactionRepository).save(txCaptor.capture());

                assertThat(txCaptor.getValue().type()).isEqualTo(TransactionType.WITHDRAW);
                assertThat(txCaptor.getValue().amount()).isEqualTo(500L);
                assertThat(txCaptor.getValue().balanceBefore()).isEqualTo(1000L);
                assertThat(txCaptor.getValue().balanceAfter()).isEqualTo(500L);
        }

        @Test
        void 출금_한도() {
                AccountRepository.Account account = new AccountRepository.Account(
                                1L, ACCOUNT_NUMBER, 2000000L, AccountStatus.ACTIVE);
                given(accountRepository.findForUpdate(ACCOUNT_NUMBER)).willReturn(Optional.of(account));
                given(transactionRepository.sumAmountByTypeBetween(
                                eq(ACCOUNT_NUMBER), eq(TransactionType.WITHDRAW), any(Instant.class),
                                any(Instant.class)))
                                .willReturn(900000L);

                assertThatThrownBy(() -> transferService.withdraw(ACCOUNT_NUMBER, 200000L))
                                .isInstanceOf(BusinessException.class)
                                .extracting(e -> ((BusinessException) e).getErrorCode())
                                .isEqualTo(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }

        @Test
        void 이체() {
                AccountRepository.Account fromAccount = new AccountRepository.Account(
                                1L, ACCOUNT_NUMBER, 10000L, AccountStatus.ACTIVE);
                AccountRepository.Account toAccount = new AccountRepository.Account(
                                2L, COUNTER_ACCOUNT_NUMBER, 0L, AccountStatus.ACTIVE);

                given(accountRepository.findForUpdate(ACCOUNT_NUMBER)).willReturn(Optional.of(fromAccount));
                given(accountRepository.findForUpdate(COUNTER_ACCOUNT_NUMBER)).willReturn(Optional.of(toAccount));
                given(transactionRepository.sumAmountByTypeBetween(
                                eq(ACCOUNT_NUMBER), eq(TransactionType.TRANSFER_OUT), any(Instant.class),
                                any(Instant.class)))
                                .willReturn(0L);

                TransferService.TransferResult result = transferService.transfer(ACCOUNT_NUMBER, COUNTER_ACCOUNT_NUMBER,
                                1000L);

                assertThat(result.fee()).isEqualTo(10L);
                assertThat(result.fromBalance()).isEqualTo(8990L);
                assertThat(result.toBalance()).isEqualTo(1000L);

                ArgumentCaptor<TransactionRepository.Tx> txCaptor = ArgumentCaptor
                                .forClass(TransactionRepository.Tx.class);
                verify(transactionRepository, times(2)).save(txCaptor.capture());

                List<TransactionRepository.Tx> txs = txCaptor.getAllValues();
                assertThat(txs).anyMatch(tx -> tx.type() == TransactionType.TRANSFER_OUT);
                assertThat(txs).anyMatch(tx -> tx.type() == TransactionType.TRANSFER_IN);
        }

        @Test
        void 이체_한도() {
                AccountRepository.Account fromAccount = new AccountRepository.Account(
                                1L, ACCOUNT_NUMBER, 5000000L, AccountStatus.ACTIVE);
                AccountRepository.Account toAccount = new AccountRepository.Account(
                                2L, COUNTER_ACCOUNT_NUMBER, 0L, AccountStatus.ACTIVE);

                given(accountRepository.findForUpdate(ACCOUNT_NUMBER)).willReturn(Optional.of(fromAccount));
                given(accountRepository.findForUpdate(COUNTER_ACCOUNT_NUMBER)).willReturn(Optional.of(toAccount));
                given(transactionRepository.sumAmountByTypeBetween(
                                eq(ACCOUNT_NUMBER), eq(TransactionType.TRANSFER_OUT), any(Instant.class),
                                any(Instant.class)))
                                .willReturn(2900000L);

                assertThatThrownBy(() -> transferService.transfer(ACCOUNT_NUMBER, COUNTER_ACCOUNT_NUMBER, 200000L))
                                .isInstanceOf(BusinessException.class)
                                .extracting(e -> ((BusinessException) e).getErrorCode())
                                .isEqualTo(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }
}