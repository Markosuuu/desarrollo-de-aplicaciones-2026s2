package com.prontaentrega.services.exceptions.jugadores;

/**
 * Error producido cuando la fuente externa no entrega datos utilizables.
 */
public class ProveedorNoDisponibleException extends RuntimeException {
    private final String code;
    /**
     * Construye una excepcion de proveedor con codigo estable para la API.
     */
    public ProveedorNoDisponibleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
