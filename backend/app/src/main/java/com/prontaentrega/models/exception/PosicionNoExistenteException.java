package com.prontaentrega.models.exception;

public class PosicionNoExistenteException extends RuntimeException {
    public PosicionNoExistenteException(String message) {
        super(message);
    }
}
