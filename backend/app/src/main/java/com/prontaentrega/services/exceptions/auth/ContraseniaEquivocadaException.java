package com.prontaentrega.services.exceptions.auth;

public class ContraseniaEquivocadaException extends RuntimeException {
    public ContraseniaEquivocadaException(String message) {
        super(message);
    }
}
