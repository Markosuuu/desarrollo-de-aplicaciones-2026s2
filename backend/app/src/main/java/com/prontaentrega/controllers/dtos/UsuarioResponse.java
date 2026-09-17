package com.prontaentrega.controllers.dtos;

import com.prontaentrega.models.Usuario;
import java.util.UUID;

public record UsuarioResponse(UUID id, String nombre, String correo) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo());
    }
}
