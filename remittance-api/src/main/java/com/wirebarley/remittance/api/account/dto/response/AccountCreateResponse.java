package com.wirebarley.remittance.api.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계좌 생성 응답")
public record AccountCreateResponse(
                @Schema(description = "계좌번호", example = "111-111-111") String accountNumber) {
}
