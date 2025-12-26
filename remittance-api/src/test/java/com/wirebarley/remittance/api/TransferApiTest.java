package com.wirebarley.remittance.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.wirebarley.remittance.domain.repository.AccountRepository;

public class TransferApiTest extends IntegrationTestBase {

    private static final Logger log = LoggerFactory.getLogger(TransferApiTest.class);

    @Autowired
    AccountRepository accountRepository;

    @Test
    void 입금() throws Exception {
        String accountNumber = "111-111-111";
        long amount = 500L;

        createAccount(accountNumber);

        mockMvc.perform(post("/api/transfer/deposit/{accountNumber}", accountNumber)
                .contentType(JSON)
                .content(objectMapper.writeValueAsString(Map.of("amount", amount))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.data.balance").value(amount));

        AccountRepository.Account account = accountRepository.findForUpdate(accountNumber).orElseThrow();
        log.info("입금() -> account balance: {}", account.balance());
        assertThat(amount).isEqualTo(account.balance());
    }

    @Test
    void 출금() throws Exception {
        String accountNumber = "111-111-111";
        long depositAmount = 1000L;
        long withdrawAmount = 500L;
        long expected = depositAmount - withdrawAmount;

        createAccount(accountNumber);
        deposit(accountNumber, depositAmount);

        mockMvc.perform(post("/api/transfer/withdraw/{accountNumber}", accountNumber)
                .contentType(JSON)
                .content(objectMapper.writeValueAsString(Map.of("amount", withdrawAmount))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(depositAmount - withdrawAmount));

        AccountRepository.Account account = accountRepository.findForUpdate(accountNumber).orElseThrow();
        log.info("출금() -> account balance: {}", account.balance());
        assertThat(expected).isEqualTo(account.balance());
    }

    @Test
    void 출금_한도초과() throws Exception {
        String accountNumber = "111-111-111";
        long depositAmount = 2000000L;
        long withdrawAmount = 1100000L;

        createAccount(accountNumber);
        deposit(accountNumber, depositAmount);

        mockMvc.perform(post("/api/transfer/withdraw/{accountNumber}", accountNumber)
                .contentType(JSON)
                .content(objectMapper.writeValueAsString(Map.of("amount", withdrawAmount))))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").exists());
    }

    @Test
    void 이체() throws Exception {
        String fromAccountNumber = "111-111-111";
        String toAccountNumber = "111-111-112";
        long depositAmount = 10000L;
        long transferAmount = 1000L;
        long fee = transferAmount / 100;
        long expectedFromBalance = depositAmount - transferAmount - fee;
        long expectedToBalance = transferAmount;

        createAccount(fromAccountNumber);
        createAccount(toAccountNumber);
        deposit(fromAccountNumber, depositAmount);

        mockMvc.perform(post("/api/transfer/transfers")
                .contentType(JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "fromAccount", fromAccountNumber,
                        "toAccount", toAccountNumber,
                        "amount", transferAmount))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fromAccountNumber").value(fromAccountNumber))
                .andExpect(jsonPath("$.data.toAccountNumber").value(toAccountNumber))
                .andExpect(jsonPath("$.data.fee").value(fee))
                .andExpect(jsonPath("$.data.fromBalance").value(expectedFromBalance))
                .andExpect(jsonPath("$.data.toBalance").value(expectedToBalance));

        AccountRepository.Account fromAccount = accountRepository.findForUpdate(fromAccountNumber).orElseThrow();
        AccountRepository.Account toAccount = accountRepository.findForUpdate(toAccountNumber).orElseThrow();
        log.info("이체() -> from account balance: {}, to account balance: {}", fromAccount.balance(),
                toAccount.balance());
        assertThat(expectedFromBalance).isEqualTo(fromAccount.balance());
        assertThat(expectedToBalance).isEqualTo(toAccount.balance());
    }

    @Test
    void 이체_한도초과() throws Exception {
        String fromAccountNumber = "111-111-111";
        String toAccountNumber = "111-111-112";
        long depositAmount = 5000000L;
        long transferAmount = 3100000L;

        createAccount(fromAccountNumber);
        createAccount(toAccountNumber);
        deposit(fromAccountNumber, depositAmount);

        mockMvc.perform(post("/api/transfer/transfers")
                .contentType(JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "fromAccount", fromAccountNumber,
                        "toAccount", toAccountNumber,
                        "amount", transferAmount))))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").exists());
    }

    @Test
    void 이체_동시성() throws Exception {
        String fromAccount = "111-111-111";
        String toAccount = "111-111-112";

        createAccount(fromAccount);
        createAccount(toAccount);

        long amount = 1000L;
        long fee = amount / 100;

        int k = 50; // 성공 해야할 요청 수
        int n = 100; // 요청 수

        deposit(fromAccount, k * (amount + fee));

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(n);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        for (int i = 0; i < n; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail.incrementAndGet();
                    done.countDown();
                    return;
                }

                try {
                    MvcResult result = mockMvc.perform(post("/api/transfer/transfers")
                            .contentType(JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "fromAccount", fromAccount,
                                    "toAccount", toAccount,
                                    "amount", amount))))
                            .andReturn();

                    String body = result.getResponse().getContentAsString();
                    JsonNode root = objectMapper.readTree(body);
                    boolean isOk = root.path("success").asBoolean(false);

                    if (isOk)
                        success.incrementAndGet();
                    else
                        fail.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await(); // 모든 스레드 준비 대기
        start.countDown(); // 동시에 시작
        done.await(); // 모든 스레드 완료 대기
        pool.shutdown(); // 스레드풀 종료

        assertThat(k).isEqualTo(success.get());
        long fromBalance = accountRepository.findForUpdate(fromAccount).orElseThrow().balance();
        long toBalance = accountRepository.findForUpdate(toAccount).orElseThrow().balance();

        log.info("이체_동시성() -> success: {}, fail: {}, from: {}, to: {}", success.get(), fail.get(), fromBalance,
                toBalance);

        assertThat(0L).isEqualTo(fromBalance);
        assertThat(k * amount).isEqualTo(toBalance);
    }

    @Test
    void 이체_교차() throws Exception {
        String fromAccount = "111-111-111";
        String toAccount = "111-111-112";
        long depositAmount = 1000000L;

        createAccount(fromAccount);
        createAccount(toAccount);

        deposit(fromAccount, depositAmount);
        deposit(toAccount, depositAmount);

        int n = 50; // 요청 수

        long amount = 1000L;
        long fee = amount / 100;

        ExecutorService pool = Executors.newFixedThreadPool(2);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        for (int i = 0; i < n; i++) {
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);

            Future<Boolean> f1 = pool.submit(() -> {
                ready.countDown();
                start.await();
                return transferOk(fromAccount, toAccount, amount);
            });

            Future<Boolean> f2 = pool.submit(() -> {
                ready.countDown();
                start.await();
                return transferOk(toAccount, fromAccount, amount);
            });

            ready.await();
            start.countDown();

            if (f1.get(2, TimeUnit.SECONDS))
                success.incrementAndGet();
            else
                fail.incrementAndGet();

            if (f2.get(2, TimeUnit.SECONDS))
                success.incrementAndGet();
            else
                fail.incrementAndGet();
        }

        pool.shutdown();

        long fromBalance = accountRepository.findForUpdate(fromAccount).orElseThrow().balance();
        long toBalance = accountRepository.findForUpdate(toAccount).orElseThrow().balance();

        long expectedSum = depositAmount * 2 - (success.get() * fee);

        log.info("이체_교차() -> success: {}, fail: {}, from: {}, to: {}", success.get(),
                fail.get(), fromBalance,
                toBalance);

        assertThat(expectedSum).isEqualTo(fromBalance + toBalance);
        assertThat(fromBalance).isEqualTo(toBalance);
    }

    private boolean transferOk(String from, String to, long amount) {
        try {
            MvcResult result = mockMvc.perform(post("/api/transfer/transfers")
                    .contentType(JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                            "fromAccount", from,
                            "toAccount", to,
                            "amount", amount))))
                    .andReturn();

            JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
            return root.path("success").asBoolean(false);
        } catch (Exception e) {
            return false;
        }
    }
}
