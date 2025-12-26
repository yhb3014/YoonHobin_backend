package com.wirebarley.remittance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.wirebarley.remittance")
@EntityScan(basePackages = "com.wirebarley.remittance")
@EnableJpaRepositories(basePackages = "com.wirebarley.remittance")
public class RemittanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RemittanceApplication.class, args);
    }
}
