package com.wirebarley.remittance.api.transfer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Schema(description = "이체 요청")
public record TransferRequest(
                @Schema(description = "출금 계좌번호", example = "111-111-111", requiredMode = RequiredMode.REQUIRED) @NotBlank String fromAccount,
                @Schema(description = "입금 계좌번호", example = "111-111-112", requiredMode = RequiredMode.REQUIRED) @NotBlank String toAccount,
                @Schema(description = "이체 금액", example = "5000", requiredMode = RequiredMode.REQUIRED) @Positive long amount) {

}
