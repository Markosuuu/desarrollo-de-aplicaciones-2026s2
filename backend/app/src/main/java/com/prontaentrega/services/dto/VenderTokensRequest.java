package com.prontaentrega.services.dto;

import java.util.UUID;

public record VenderTokensRequest(UUID jugadorId, Integer cantidad) {
}
