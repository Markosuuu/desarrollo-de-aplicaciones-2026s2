package com.prontaentrega.services.exceptions.auth;

public class CampoInvalidoException extends RuntimeException {
    public CampoInvalidoException(String message) {
        super(message);
    }
}
