package com.prontaentrega.services.exceptions.cotizacion;

/** Señala que no hay estado o configuración suficiente para ejecutar una operación de quotes. */
public class NoHayEstrategiaActivaConfiguradaException extends RuntimeException {
    /** Crea el conflicto con un mensaje apto para la respuesta funcional. */
    public NoHayEstrategiaActivaConfiguradaException(String message) {
        super(message);
    }
}