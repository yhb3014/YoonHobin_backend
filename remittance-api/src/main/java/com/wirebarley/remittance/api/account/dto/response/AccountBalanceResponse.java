package com.wirebarley.remittance.api.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계좌 잔액 응답")
public record AccountBalanceResponse(
        @Schema(description = "계좌번호", example = "111-111-111") String accountNumber,
        @Schema(description = "잔액", example = "100000") long balance) {

}
