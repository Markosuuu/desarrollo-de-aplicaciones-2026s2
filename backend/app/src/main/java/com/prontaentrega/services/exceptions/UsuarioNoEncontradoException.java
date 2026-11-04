package com.prontaentrega.services.exceptions;

import java.util.UUID;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
    public UsuarioNoEncontradoException(UUID id) {
        super("Usuario no encontrado con el id: " + id);
    }
}
