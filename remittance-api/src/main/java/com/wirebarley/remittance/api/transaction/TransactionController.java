package com.wirebarley.remittance.api.transaction;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wirebarley.remittance.api.transaction.dto.request.TransactionItem;
import com.wirebarley.remittance.api.transaction.dto.response.TransactionListResponse;
import com.wirebarley.remittance.application.TransactionAppService;
import com.wirebarley.remittance.common.response.ApiResponseDto;
import com.wirebarley.remittance.domain.repository.TransactionRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Transaction", description = "거래 내역")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transaction")
public class TransactionController {

        private final TransactionAppService transactionAppService;

        @Operation(summary = "거래 내역 조회")
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TransactionListResponse.class)))
        @GetMapping("/{accountNumber}")
        public ApiResponseDto<TransactionListResponse> getTransactions(
                        @Parameter(description = "계좌번호", example = "111-111-111", required = true) @PathVariable String accountNumber,
                        @Parameter(description = "페이지", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size) {
                if (size <= 0)
                        size = 20;

                List<TransactionRepository.Tx> transactions = transactionAppService.getTransactions(accountNumber, page,
                                size);

                ZoneId zone = ZoneId.of("Asia/Seoul");
                List<TransactionItem> items = transactions.stream()
                                .map(t -> new TransactionItem(
                                                t.type(),
                                                t.amount(),
                                                t.feeAmount(),
                                                t.counterpartyAccountNumber(),
                                                t.balanceBefore(),
                                                t.balanceAfter(),
                                                t.groupId(),
                                                t.createdAt().atZone(zone).toOffsetDateTime()))
                                .toList();

                return ApiResponseDto.ok(new TransactionListResponse(accountNumber, page, size, items));
        }
}
