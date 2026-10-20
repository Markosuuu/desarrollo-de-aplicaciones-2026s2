package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.TokenEstado;

import java.util.Optional;
import java.util.UUID;

public interface TokenEstadoRepository {
    TokenEstado save(TokenEstado tokenEstado);
    void deleteAll();
    Optional<TokenEstado> findByUsuarioId(UUID usuarioId);
}
