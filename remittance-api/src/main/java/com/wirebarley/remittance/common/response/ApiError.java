package com.wirebarley.remittance.common.response;

public record ApiError(
        String code,
        String message) {

}
