package com.prontaentrega.controllers.dtos.mercado;

import java.util.UUID;

public record VenderTokensRequest(UUID jugadorId, Integer cantidad) {
}
