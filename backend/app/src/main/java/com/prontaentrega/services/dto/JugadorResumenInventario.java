package com.prontaentrega.services.dto;

import java.util.UUID;

public record JugadorResumenInventario(
        UUID jugadorId,
        String jugadorNombre,
        Long cantidad
) {
}
