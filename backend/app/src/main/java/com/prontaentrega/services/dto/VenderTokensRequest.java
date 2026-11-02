package com.prontaentrega.services.dto;

import java.util.UUID;

// TODO: Mover a controller
public record VenderTokensRequest(UUID jugadorId, Integer cantidad) {
}
