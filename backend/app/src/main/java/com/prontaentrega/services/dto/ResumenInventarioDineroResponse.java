package com.prontaentrega.services.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumenInventarioDineroResponse(
        BigDecimal total,
        List<JugadorResumenValorInventario> jugadores
) {}
