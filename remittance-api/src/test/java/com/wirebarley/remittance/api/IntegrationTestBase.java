package com.wirebarley.remittance.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Import(PostgresTestContainerConfig.class)
public abstract class IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clean() {
        jdbcTemplate.execute("TRUNCATE TABLE transactions RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE accounts RESTART IDENTITY CASCADE");
    }

    final MediaType JSON = MediaType.APPLICATION_JSON;

    void createAccount(String accountNumber) throws Exception {
        mockMvc.perform(post("/api/account")
                .contentType(JSON)
                .content(objectMapper.writeValueAsString(Map.of("accountNumber", accountNumber))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    void deposit(String accountNumber, long amount) throws Exception {
        mockMvc.perform(post("/api/transfer/deposit/{accountNumber}", accountNumber)
                .contentType(JSON)
                .content(objectMapper.writeValueAsString(Map.of("amount", amount))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    void withdraw(String accountNumber, long amount) throws Exception {
        mockMvc.perform(post("/api/transfer/withdraw/{accountNumber}", accountNumber)
                .contentType(JSON)
                .content(objectMapper.writeValueAsString(Map.of("amount", amount))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    void transfer(String from, String to, long amount) throws Exception {
        mockMvc.perform(post("/api/transfer/transfers")
                .contentType(JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "fromAccount", from,
                        "toAccount", to,
                        "amount", amount))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
