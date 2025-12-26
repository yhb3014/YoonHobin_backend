package com.wirebarley.remittance.common.response;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record ApiResponseDto<T>(
        boolean success,
        T data,
        ApiError error,
        OffsetDateTime timestamp) {
    public static <T> ApiResponseDto<T> ok(T data) {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        return new ApiResponseDto<>(true, data, null, OffsetDateTime.now(zone));
    }

    public static <T> ApiResponseDto<T> fail(String code, String message) {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        return new ApiResponseDto<>(false, null, new ApiError(code, message), OffsetDateTime.now(zone));
    }
}
