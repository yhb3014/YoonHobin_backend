package com.wirebarley.remittance.common.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Wirebarley TEST", version = "v1", description = "송금 서비스 코딩테스트"))
public class SwaggerConfig {

}
