package com.prontaentrega.services.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record JugadorResumenValorInventario(
        UUID jugadorId,
        String jugadorNombre,
        Long cantidad,
        BigDecimal valor
) {}