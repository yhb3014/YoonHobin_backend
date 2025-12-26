package com.wirebarley.remittance.domain.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.wirebarley.remittance.domain.account.AccountStatus;
import com.wirebarley.remittance.domain.policy.DailyLimitPolicy;
import com.wirebarley.remittance.domain.policy.FeePolicy;
import com.wirebarley.remittance.domain.repository.AccountRepository;
import com.wirebarley.remittance.domain.repository.TransactionRepository;
import com.wirebarley.remittance.domain.transaction.TransactionType;
import com.wirebarley.remittance.domain.util.TimeRanges;
import com.wirebarley.remittance.exception.BusinessException;
import com.wirebarley.remittance.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private final Clock clock;
    private final DailyLimitPolicy dailyLimitPolicy;
    private final FeePolicy feePolicy;

    /**
     * 입금
     * 
     * @param accountNumber
     * @param amount
     * @return 입금 이후의 잔액
     */
    public long deposit(String accountNumber, long amount) {
        validateAmount(amount);

        AccountRepository.Account account = accountRepository
                .findForUpdate(accountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "없는 계좌입니다."));

        validateAccount(account);

        long before = account.balance();
        long after = before + amount;
        accountRepository.save(
                new AccountRepository.Account(account.id(), account.accountNumber(), after, account.status()));
        transactionRepository
                .save(new TransactionRepository.Tx(null, account.id(), account.accountNumber(), TransactionType.DEPOSIT,
                        amount, 0L, null, null, before, after, null, now()));

        return after;
    }

    /**
     * 출금
     * 
     * @param accountNumber
     * @param amount
     * @return 출금 이후의 잔액
     */
    public long withdraw(String accountNumber, long amount) {
        validateAmount(amount);

        AccountRepository.Account account = accountRepository
                .findForUpdate(accountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "없는 계좌입니다."));

        validateAccount(account);
        checkDailyLimit(accountNumber, TransactionType.WITHDRAW, amount);

        if (account.balance() < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE, "잔액이 부족합니다.");
        }

        long before = account.balance();
        long after = before - amount;
        accountRepository.save(
                new AccountRepository.Account(account.id(), account.accountNumber(), after, account.status()));
        transactionRepository.save(
                new TransactionRepository.Tx(null, account.id(), account.accountNumber(), TransactionType.WITHDRAW,
                        amount, 0L, null, null, before, after, null, now()));

        return after;
    }

    /**
     * 이체
     * 
     * @param fromAccountNumber
     * @param toAccountNumber
     * @param amount
     * @return 이체 후 결과
     */
    public TransferResult transfer(String fromAccountNumber, String toAccountNumber, long amount) {
        validateAmount(amount);
        if (fromAccountNumber == null || toAccountNumber == null
                || fromAccountNumber.isBlank() || toAccountNumber.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "계좌 번호를 입력해주세요.");
        }

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "같은 계좌로는 이체가 불가능합니다.");
        }

        /**
         * A -> B / B -> A 데드락 방지
         * 순서 정렬을 통해 락 획득
         * 
         * https://www.postgresql.org/docs/current/explicit-locking.html
         * https://notavoid.tistory.com/119
         * 
         * CASE 1로 테스트 했을 때, while locking tuple (0,3) in relation "accounts" Lock 확인
         */
        // CASE 1
        // AccountRepository.Account from =
        // accountRepository.findForUpdate(fromAccountNumber)
        // .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "없는
        // 계좌입니다."));
        // AccountRepository.Account to =
        // accountRepository.findForUpdate(toAccountNumber)
        // .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "없는
        // 계좌입니다."));

        // CASE 2
        String first = fromAccountNumber.compareTo(toAccountNumber) <= 0 ? fromAccountNumber : toAccountNumber;
        String second = first.equals(fromAccountNumber) ? toAccountNumber : fromAccountNumber;

        AccountRepository.Account firstAccount = accountRepository.findForUpdate(first)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "없는 계좌입니다."));
        AccountRepository.Account secondAccount = accountRepository.findForUpdate(second)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "없는 계좌입니다."));
        AccountRepository.Account from = firstAccount.accountNumber().equals(fromAccountNumber) ? firstAccount
                : secondAccount;
        AccountRepository.Account to = secondAccount.accountNumber().equals(toAccountNumber)
                ? secondAccount
                : firstAccount;

        validateAccount(from);
        validateAccount(to);

        checkDailyLimit(fromAccountNumber, TransactionType.TRANSFER_OUT, amount);

        long fee = feePolicy.feeOf(amount);
        long totalDebit = amount + fee;

        if (from.balance() < totalDebit) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE, "잔액이 부족합니다.");
        }

        long fromNew = from.balance() - totalDebit;
        long toNew = to.balance() + amount;

        accountRepository.save(new AccountRepository.Account(from.id(), from.accountNumber(), fromNew, from.status()));
        accountRepository.save(new AccountRepository.Account(to.id(), to.accountNumber(), toNew, to.status()));

        UUID groupId = UUID.randomUUID();
        Instant createdAt = now();

        transactionRepository.save(new TransactionRepository.Tx(
                null, from.id(), from.accountNumber(), TransactionType.TRANSFER_OUT, amount, fee, to.id(),
                to.accountNumber(), from.balance(), fromNew, groupId,
                createdAt));
        transactionRepository.save(new TransactionRepository.Tx(
                null, to.id(), to.accountNumber(), TransactionType.TRANSFER_IN, amount, 0L, from.id(),
                from.accountNumber(), to.balance(), toNew, groupId,
                createdAt));

        return new TransferResult(from.accountNumber(), fromNew, to.accountNumber(), toNew, fee);
    }

    public record TransferResult(
            String fromAccountNumber,
            long fromBalance,
            String toAccountNumber,
            long toBalance,
            long fee) {
    }

    private void validateAmount(long amount) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "금액을 정확히 입력해주세요.");
        }
    }

    private void validateAccount(AccountRepository.Account account) {
        if (account.status() == AccountStatus.CLOSED) {
            throw new BusinessException(ErrorCode.ACCOUNT_CLOSED, "닫힌 계좌입니다.");
        }
    }

    private void checkDailyLimit(String accountNumber, TransactionType type, long addAmount) {
        Instant[] range = TimeRanges.todayRange(clock);
        long todaySum = transactionRepository.sumAmountByTypeBetween(accountNumber, type, range[0], range[1]);
        long limit = dailyLimitPolicy.limitOf(type);

        if (todaySum + addAmount > limit) {
            String msg = switch (type) {
                case WITHDRAW -> "출금 한도가 초과되었습니다.";
                case TRANSFER_OUT -> "이체 한도가 초과되었습니다.";
                default -> "한도가 초과되었습니다.";
            };

            throw new BusinessException(ErrorCode.DAILY_LIMIT_EXCEEDED, msg);
        }
    }

    private Instant now() {
        return Instant.now(clock);
    }

}