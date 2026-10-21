package com.prontaentrega.controllers.dto.quotes;

/** Confirmación de configuración persistida para una estrategia. */
public record QuoteStrategyConfigResponse(String strategyId, Object config) {
}