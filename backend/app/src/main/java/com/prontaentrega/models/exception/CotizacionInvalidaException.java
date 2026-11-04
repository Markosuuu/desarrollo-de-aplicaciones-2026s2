package com.prontaentrega.models.exception;

/** Señala que una cotización propuesta incumple la invariante del mercado. */
public class CotizacionInvalidaException extends RuntimeException {
    /** Crea el error que debe revertir la recotización completa. */
    public CotizacionInvalidaException(String message) {
        super(message);
    }
}