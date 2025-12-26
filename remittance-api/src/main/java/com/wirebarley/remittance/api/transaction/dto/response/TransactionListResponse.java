package com.wirebarley.remittance.api.transaction.dto.response;

import java.util.List;

import com.wirebarley.remittance.api.transaction.dto.request.TransactionItem;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "거래 내역 조회 응답")
public record TransactionListResponse(
                @Schema(description = "계좌번호", example = "111-111-111") String accountNumber,
                @Schema(description = "페이지", example = "0") int page,
                @Schema(description = "사이즈", example = "20") int size,
                @Schema(description = "거래 목록") List<TransactionItem> items) {

}
