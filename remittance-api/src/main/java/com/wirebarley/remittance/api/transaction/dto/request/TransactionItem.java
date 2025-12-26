package com.wirebarley.remittance.api.transaction.dto.request;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.wirebarley.remittance.domain.transaction.TransactionType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "거래 내역 아이템")
public record TransactionItem(
        @Schema(description = "거래 타입", example = "DEPOSIT") TransactionType type,
        @Schema(description = "거래 금액", example = "10000") long amount,
        @Schema(description = "수수료", example = "0") long feeAmount,
        @Schema(description = "상대 계좌번호", example = "111-111-111", nullable = true) String counterpartyAccountNumber,
        @Schema(description = "거래 전 잔액", example = "5000") long balanceBefore,
        @Schema(description = "거래 후 잔액", example = "15000") long balanceAfter,
        @Schema(description = "그룹 ID", example = "73f754c1-91c7-49ce-8f93-350a6aeb6e38", nullable = true) UUID groupId,
        @Schema(description = "생성 시각", example = "2025-12-01T00:00:00.463888+09:00") OffsetDateTime createdAt) {

}
