package com.prontaentrega.controllers.dto.quotes;

import java.util.List;

/** Colección pública de las estrategias disponibles. */
public record QuoteStrategiesResponse(List<QuoteStrategyResponse> strategies) {
}