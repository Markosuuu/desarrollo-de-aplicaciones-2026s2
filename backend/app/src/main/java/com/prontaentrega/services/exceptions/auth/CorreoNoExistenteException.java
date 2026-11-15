package com.prontaentrega.services.exceptions.auth;

public class CorreoNoExistenteException extends RuntimeException {
    public CorreoNoExistenteException(String message) {
        super(message);
    }
}
