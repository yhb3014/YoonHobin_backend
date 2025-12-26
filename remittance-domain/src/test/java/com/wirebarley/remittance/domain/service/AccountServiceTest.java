package com.wirebarley.remittance.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wirebarley.remittance.domain.account.AccountStatus;
import com.wirebarley.remittance.domain.repository.AccountRepository;
import com.wirebarley.remittance.exception.BusinessException;
import com.wirebarley.remittance.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private static final String ACCOUNT_NUMBER = "111-111-111";

    @Test
    void 계좌등록() {
        given(accountRepository.existsByAccountNumber(ACCOUNT_NUMBER)).willReturn(false);

        accountService.register(ACCOUNT_NUMBER);

        ArgumentCaptor<AccountRepository.Account> captor = ArgumentCaptor.forClass(AccountRepository.Account.class);
        verify(accountRepository).save(captor.capture());
        AccountRepository.Account saved = captor.getValue();

        assertThat(saved.accountNumber()).isEqualTo(ACCOUNT_NUMBER);
        assertThat(saved.balance()).isZero();
        assertThat(saved.status()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void 계좌등록_중복() {
        given(accountRepository.existsByAccountNumber(ACCOUNT_NUMBER)).willReturn(true);

        assertThatThrownBy(() -> accountService.register(ACCOUNT_NUMBER))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_ACCOUNT_NUMBER);
    }

    @Test
    void 계좌삭제() {
        AccountRepository.Account active = new AccountRepository.Account(
                1L, ACCOUNT_NUMBER, 0L, AccountStatus.ACTIVE);
        given(accountRepository.findForUpdate(ACCOUNT_NUMBER)).willReturn(Optional.of(active));

        accountService.delete(ACCOUNT_NUMBER);

        ArgumentCaptor<AccountRepository.Account> captor = ArgumentCaptor.forClass(AccountRepository.Account.class);
        verify(accountRepository).save(captor.capture());

        assertThat(captor.getValue().status()).isEqualTo(AccountStatus.CLOSED);
    }
}