package com.prontaentrega.controllers.dtos.transacciones;

public record CancelarVentaResponse(boolean success, String message) {
    public static CancelarVentaResponse from(boolean success, String message) {
        return new CancelarVentaResponse(success, message);
    }
}
