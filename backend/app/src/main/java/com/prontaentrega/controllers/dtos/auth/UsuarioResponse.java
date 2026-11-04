package com.prontaentrega.controllers.dtos.auth;

import com.prontaentrega.models.Usuario;
import java.math.BigDecimal;
import java.util.UUID;

public record UsuarioResponse(UUID id, String nombre, String correo, BigDecimal creditosDisponibles) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getCreditosDisponibles());
    }
}
