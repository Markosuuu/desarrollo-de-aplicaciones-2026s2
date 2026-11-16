package com.prontaentrega.services.exceptions.cotizacion;

/** Indica que el ID solicitado no corresponde a una estrategia registrada. */
public class EstrategiaNoExistenteException extends RuntimeException {
    /** Crea un error de estrategia inexistente. */
    public EstrategiaNoExistenteException(String message) {
        super(message);
    }
}