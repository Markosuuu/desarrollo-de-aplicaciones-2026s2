package com.prontaentrega.services.exceptions;

/** Señala que no hay estado o configuración suficiente para ejecutar una operación de quotes. */
public class QuoteOperationConflictException extends RuntimeException {
    /** Crea el conflicto con un mensaje apto para la respuesta funcional. */
    public QuoteOperationConflictException(String message) {
        super(message);
    }
}