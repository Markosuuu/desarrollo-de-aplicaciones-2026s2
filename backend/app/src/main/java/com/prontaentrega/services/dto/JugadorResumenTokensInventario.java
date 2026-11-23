package com.prontaentrega.services.dto;

import java.util.UUID;

public record JugadorResumenTokensInventario(
        UUID jugadorId,
        String jugadorNombre,
        Long cantidad
) {
}
