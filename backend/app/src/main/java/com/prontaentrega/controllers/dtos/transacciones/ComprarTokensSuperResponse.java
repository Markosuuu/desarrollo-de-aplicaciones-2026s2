package com.prontaentrega.controllers.dtos.transacciones;

import java.util.UUID;

public record ComprarTokensSuperResponse(boolean success, String message, UUID jugadorId, Integer cantidad) {
    public static ComprarTokensSuperResponse from(boolean success, String message, UUID jugadorId, Integer cantidad) {
        return new ComprarTokensSuperResponse(success, message, jugadorId, cantidad);
    }
}
