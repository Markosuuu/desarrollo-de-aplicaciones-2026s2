package com.prontaentrega.services.dto;

import com.prontaentrega.models.Usuario;

/**
 * Resultado de operaciones de autenticación en la capa de servicios.
 */
public record AuthResult(Usuario usuario, String token) {
}
