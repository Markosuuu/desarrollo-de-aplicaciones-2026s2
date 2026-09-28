package com.prontaentrega.controllers.dtos;

import com.prontaentrega.services.dto.AuthResult;

public record AuthResponse(UsuarioResponse usuario, String token) {
    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(
                UsuarioResponse.from(result.usuario()),
                result.token());
    }
}