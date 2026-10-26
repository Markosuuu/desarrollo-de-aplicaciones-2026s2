package com.prontaentrega.controllers.exceptions;

public record ErrorResponse(
        ErrorDetail error
) {
    public record ErrorDetail(
            String code,
            String message
    ) {}
}
