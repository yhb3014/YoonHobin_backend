package com.wirebarley.remittance.api.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "계좌 생성 요청")
public record AccountCreateRequest(
        @Schema(description = "계좌번호", example = "111-111-111", requiredMode = RequiredMode.REQUIRED) @NotBlank String accountNumber) {
}
