package com.wirebarley.remittance.api.transfer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "잔액 응답")
public record BalanceResponse(
        @Schema(description = "계좌번호", example = "111-111-111") String accountNumber,
        @Schema(description = "잔액", example = "15000") long balance) {

}
