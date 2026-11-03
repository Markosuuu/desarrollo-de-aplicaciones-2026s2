package com.prontaentrega.controllers.dtos.transacciones;

import java.util.UUID;

public record ComprarTokensResponse(boolean success, String message, UUID anuncioId) {
    public static ComprarTokensResponse from(boolean success, String message, UUID anuncioId) {
        return new ComprarTokensResponse(success, message, anuncioId);
    }
}
