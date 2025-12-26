package com.wirebarley.remittance.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.wirebarley.remittance.common.response.ApiResponseDto;
import com.wirebarley.remittance.exception.BusinessException;
import com.wirebarley.remittance.exception.ErrorCode;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseDto<Void>> businessException(BusinessException e) {
        HttpStatus status = mapStatus(e.getErrorCode());
        return ResponseEntity
                .status(status)
                .body(ApiResponseDto.fail(e.getErrorCode().name(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("validation error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.fail("VALIDATION_ERROR", msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> exception(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR.name(), e.getMessage()));
    }

    private HttpStatus mapStatus(ErrorCode code) {
        return switch (code) {
            case ACCOUNT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DUPLICATE_ACCOUNT_NUMBER -> HttpStatus.CONFLICT;
            case INVALID_AMOUNT -> HttpStatus.BAD_REQUEST;
            case ACCOUNT_CLOSED, INSUFFICIENT_BALANCE, DAILY_LIMIT_EXCEEDED -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
