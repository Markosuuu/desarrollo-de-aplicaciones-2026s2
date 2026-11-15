package com.prontaentrega.services.exceptions;

public class UsuarioRepetidoException extends RuntimeException {
    public UsuarioRepetidoException(String message) {
        super(message);
    }
}
