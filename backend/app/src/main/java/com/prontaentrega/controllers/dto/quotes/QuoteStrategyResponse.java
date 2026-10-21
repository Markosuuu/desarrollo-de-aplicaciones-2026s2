package com.prontaentrega.controllers.dto.quotes;

/** Vista pública de una estrategia y su configuración persistida opcional. */
public record QuoteStrategyResponse(
        String strategyId,
        String strategyName,
        boolean active,
        Object config) {
}