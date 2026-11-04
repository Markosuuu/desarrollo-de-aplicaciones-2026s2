package com.prontaentrega.controllers.dtos.cotizacion;

import java.util.List;

/** Colección pública de las estrategias disponibles. */
public record CotizacionStrategiesResponse(List<CotizacionStrategyResponse> strategies) {
}