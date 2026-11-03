package com.prontaentrega.controllers.dtos.transacciones;

import java.util.UUID;

public record VenderTokensRequest(UUID jugadorId, Integer cantidad) {
}
