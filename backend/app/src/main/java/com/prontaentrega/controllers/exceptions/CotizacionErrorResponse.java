package com.prontaentrega.controllers.exceptions;

/** Error plano de la API de cotizaciones. */
public record CotizacionErrorResponse(String code, String message) {
}