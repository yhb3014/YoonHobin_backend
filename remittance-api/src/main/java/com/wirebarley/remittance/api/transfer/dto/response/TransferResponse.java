package com.wirebarley.remittance.api.transfer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이체 응답")
public record TransferResponse(

        @Schema(description = "출금 계좌번호", example = "111-111-111") String fromAccountNumber,
        @Schema(description = "출금 후 잔액", example = "9000") long fromBalance,
        @Schema(description = "입금 계좌번호", example = "111-111-112") String toAccountNumber,
        @Schema(description = "입금 후 잔액", example = "11000") long toBalance,
        @Schema(description = "수수료", example = "100") long fee) {

}
