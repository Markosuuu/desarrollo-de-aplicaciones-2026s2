package com.prontaentrega.services.exceptions;

import java.util.UUID;

public class SubastaNoEncontradaException extends RuntimeException {
    public SubastaNoEncontradaException(UUID id) {
        super("Subasta no encontrada: " + id);
    }
}
