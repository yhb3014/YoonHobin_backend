package com.wirebarley.remittance.domain.service;

import com.wirebarley.remittance.domain.account.AccountStatus;
import com.wirebarley.remittance.domain.repository.AccountRepository;
import com.wirebarley.remittance.exception.BusinessException;
import com.wirebarley.remittance.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * 계좌 등록
     * 
     * @param accountNumber
     */
    public void register(String accountNumber) {
        validateAccountNumber(accountNumber);

        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT_NUMBER, "이미 존재하는 계좌 번호 입니다.");
        }

        accountRepository.save(new AccountRepository.Account(null, accountNumber, 0L, AccountStatus.ACTIVE));
    }

    /**
     * 계좌 삭제 (Flag 변경)
     * 
     * @param accountNumber
     */
    public void delete(String accountNumber) {
        validateAccountNumber(accountNumber);

        AccountRepository.Account account = accountRepository.findForUpdate(accountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "없는 계좌 입니다."));

        if (account.status() == AccountStatus.CLOSED) {
            return;
        }

        accountRepository.save(new AccountRepository.Account(
                account.id(),
                account.accountNumber(),
                account.balance(),
                AccountStatus.CLOSED));
    }

    /**
     * 잔액 조회
     * 
     * @param accountNumber
     * @return
     */
    public long getBalance(String accountNumber) {
        validateAccountNumber(accountNumber);

        AccountRepository.Account account = accountRepository.find(accountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "없는 계좌 입니다."));

        return account.balance();
    }

    private void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "계좌 번호를 입력해주세요.");
        }
    }
}
