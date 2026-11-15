package com.prontaentrega.controllers.dtos.mercado;

import java.util.UUID;

public record ComprarTokensSuperRequest(UUID jugadorId, Integer cantidad) {
}
