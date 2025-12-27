package com.wirebarley.remittance.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

class AccountApiTest extends IntegrationTestBase {

    @Test
    void 계좌생성() throws Exception {
        String accountNumber = "111-111-111";
        mockMvc.perform(post("/api/account")
                .contentType(JSON)
                .content(objectMapper.writeValueAsString(Map.of("accountNumber", accountNumber))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value(accountNumber));
    }

    @Test
    void 계좌삭제() throws Exception {
        String accountNumber = "111-111-111";
        createAccount(accountNumber);

        mockMvc.perform(delete("/api/account/{accountNumber}", accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void 계좌잔액조회() throws Exception {
        String accountNumber = "111-111-111";
        long depositAmount = 5000L;

        createAccount(accountNumber);

        deposit(accountNumber, depositAmount);

        mockMvc.perform(get("/api/account/balance/{accountNumber}", accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.data.balance").value(depositAmount));
    }
}