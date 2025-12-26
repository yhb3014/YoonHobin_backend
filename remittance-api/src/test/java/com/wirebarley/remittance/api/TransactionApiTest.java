package com.wirebarley.remittance.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.wirebarley.remittance.domain.transaction.TransactionType;

class TransactionApiTest extends IntegrationTestBase {

    @Test
    void 거래내역() throws Exception {
        String fromAccount = "111-111-111";
        String toAccount = "111-111-112";
        createAccount(fromAccount);
        createAccount(toAccount);

        deposit(fromAccount, 10000L);
        transfer(fromAccount, toAccount, 1000L);
        withdraw(fromAccount, 500L);

        mockMvc.perform(get("/api/transaction/{accountNumber}", fromAccount)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value(fromAccount))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].type").value(TransactionType.WITHDRAW.name()));
    }
}