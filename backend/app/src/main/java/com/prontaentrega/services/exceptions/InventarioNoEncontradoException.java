package com.prontaentrega.services.exceptions;

public class InventarioNoEncontradoException extends RuntimeException {
    public InventarioNoEncontradoException(String message) {
        super(message);
    }
}
