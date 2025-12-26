package com.wirebarley.remittance.api.account;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wirebarley.remittance.api.account.dto.request.AccountCreateRequest;
import com.wirebarley.remittance.api.account.dto.response.AccountCreateResponse;
import com.wirebarley.remittance.application.AccountAppService;
import com.wirebarley.remittance.common.response.ApiResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Account", description = "계좌 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class AccountController {

    private final AccountAppService accountAppService;

    @Operation(summary = "계좌 등록")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AccountCreateResponse.class)))
    @PostMapping
    public ApiResponseDto<AccountCreateResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = AccountCreateRequest.class))) @Valid @RequestBody AccountCreateRequest request) {
        String accountNumber = request.accountNumber();
        accountAppService.register(accountNumber);
        return ApiResponseDto.ok(new AccountCreateResponse(accountNumber));
    }

    @Operation(summary = "계좌 삭제")
    @DeleteMapping("/{accountNumber}")
    public ApiResponseDto<Void> delete(
            @Parameter(description = "계좌번호", example = "111-111-111", required = true) @PathVariable String accountNumber) {
        accountAppService.delete(accountNumber);
        return ApiResponseDto.ok(null);
    }
}
