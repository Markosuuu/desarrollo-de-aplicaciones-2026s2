package com.prontaentrega.services.exceptions;

public class CorreoNoExistenteException extends RuntimeException {
    public CorreoNoExistenteException(String message) {
        super(message);
    }
}
