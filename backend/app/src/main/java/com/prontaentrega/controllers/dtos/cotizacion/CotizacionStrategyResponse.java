package com.prontaentrega.controllers.dtos.cotizacion;

/** Vista pública de una estrategia y su configuración persistida opcional. */
public record CotizacionStrategyResponse(
        String strategyId,
        String strategyName,
        boolean active,
        Object config) {
}