package com.prontaentrega.controllers.exceptionhandler;

public record ErrorResponse(
        ErrorDetail error
) {
    public record ErrorDetail(
            String code,
            String message
    ) {}
}
