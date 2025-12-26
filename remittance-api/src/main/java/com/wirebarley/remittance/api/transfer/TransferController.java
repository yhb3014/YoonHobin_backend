package com.wirebarley.remittance.api.transfer;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wirebarley.remittance.api.transfer.dto.request.AmountRequest;
import com.wirebarley.remittance.api.transfer.dto.request.TransferRequest;
import com.wirebarley.remittance.api.transfer.dto.response.BalanceResponse;
import com.wirebarley.remittance.api.transfer.dto.response.TransferResponse;
import com.wirebarley.remittance.application.TransferAppService;
import com.wirebarley.remittance.common.response.ApiResponseDto;
import com.wirebarley.remittance.domain.service.TransferService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Transfer", description = "입금/출금/이체")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transfer")
public class TransferController {

    private final TransferAppService transferAppService;

    @Operation(summary = "입금")
    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = BalanceResponse.class)))
    @PostMapping("/deposit/{accountNumber}")
    public ApiResponseDto<BalanceResponse> deposit(
            @Parameter(description = "계좌번호", example = "111-111-111", required = true) @PathVariable String accountNumber,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = AmountRequest.class))) @Valid @RequestBody AmountRequest request) {
        long balance = transferAppService.deposit(accountNumber, request.amount());
        return ApiResponseDto.ok(new BalanceResponse(accountNumber, balance));
    }

    @Operation(summary = "출금")
    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = BalanceResponse.class)))
    @PostMapping("/withdraw/{accountNumber}")
    public ApiResponseDto<BalanceResponse> withdraw(
            @Parameter(description = "계좌번호", example = "111-111-111", required = true) @PathVariable String accountNumber,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = AmountRequest.class))) @Valid @RequestBody AmountRequest request) {
        long balance = transferAppService.withdraw(accountNumber, request.amount());
        return ApiResponseDto.ok(new BalanceResponse(accountNumber, balance));
    }

    @Operation(summary = "이체")
    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = TransferResponse.class)))
    @PostMapping("/transfers")
    public ApiResponseDto<TransferResponse> transfer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = TransferRequest.class))) @Valid @RequestBody TransferRequest request) {
        TransferService.TransferResult result = transferAppService.transfer(request.fromAccount(),
                request.toAccount(),
                request.amount());
        return ApiResponseDto.ok(new TransferResponse(
                result.fromAccountNumber(),
                result.fromBalance(),
                result.toAccountNumber(),
                result.toBalance(),
                result.fee()));
    }
}
