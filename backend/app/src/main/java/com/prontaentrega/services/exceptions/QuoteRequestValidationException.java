package com.prontaentrega.services.exceptions;

/** Indica que la configuración recibida incumple una regla de entrada quote. */
public class QuoteRequestValidationException extends RuntimeException {
    /** Crea un error de validación con mensaje seguro para el cliente. */
    public QuoteRequestValidationException(String message) {
        super(message);
    }
}