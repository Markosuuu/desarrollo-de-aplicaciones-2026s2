package com.prontaentrega.models.exception;

/** Señala datos deportivos ausentes o inválidos para una estrategia concreta. */
public class DatosValoracionInvalidaException extends RuntimeException {
    /** Crea el error de datos no elegibles para valoración. */
    public DatosValoracionInvalidaException(String message) {
        super(message);
    }
}