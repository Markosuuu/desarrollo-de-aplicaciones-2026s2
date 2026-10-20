package com.prontaentrega.controllers.dto.quotes;

import jakarta.validation.constraints.NotBlank;

/** Body para seleccionar la estrategia que se activará y recotizará. */
public record QuoteStrategySelectionRequest(
        @NotBlank(message = "ESTRATEGIA_REQUERIDA") String strategyId) {
}