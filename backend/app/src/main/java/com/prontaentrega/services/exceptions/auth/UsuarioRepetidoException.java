package com.prontaentrega.services.exceptions.auth;

public class UsuarioRepetidoException extends RuntimeException {
    public UsuarioRepetidoException(String message) {
        super(message);
    }
}
