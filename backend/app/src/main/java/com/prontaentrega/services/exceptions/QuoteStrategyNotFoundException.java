package com.prontaentrega.services.exceptions;

/** Indica que el ID solicitado no corresponde a una estrategia registrada. */
public class QuoteStrategyNotFoundException extends RuntimeException {
    /** Crea un error de estrategia inexistente. */
    public QuoteStrategyNotFoundException(String message) {
        super(message);
    }
}