package com.prontaentrega.controllers.dtos.cotizacion;

import jakarta.validation.constraints.NotBlank;

/** Body para seleccionar la estrategia que se activará y recotizará. */
public record CotizacionStrategySelectionRequest(
        @NotBlank(message = "ESTRATEGIA_REQUERIDA") String strategyId) {
}