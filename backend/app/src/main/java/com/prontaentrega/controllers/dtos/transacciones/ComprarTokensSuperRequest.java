package com.prontaentrega.controllers.dtos.transacciones;

import java.util.UUID;

public record ComprarTokensSuperRequest(UUID jugadorId, Integer cantidad) {
}
