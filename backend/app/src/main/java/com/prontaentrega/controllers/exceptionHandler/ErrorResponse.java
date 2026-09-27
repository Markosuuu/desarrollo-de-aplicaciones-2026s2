package com.prontaentrega.controllers.exceptionHandler;

public record ErrorResponse(
        ErrorDetail error
) {
    public record ErrorDetail(
            String code,
            String message
    ) {}
}
