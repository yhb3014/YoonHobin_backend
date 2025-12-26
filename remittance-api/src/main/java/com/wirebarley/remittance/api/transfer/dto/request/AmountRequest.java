package com.wirebarley.remittance.api.transfer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Positive;

@Schema(description = "금액 요청")
public record AmountRequest(
        @Schema(description = "금액", example = "100000", requiredMode = RequiredMode.REQUIRED) @Positive long amount) {
}
