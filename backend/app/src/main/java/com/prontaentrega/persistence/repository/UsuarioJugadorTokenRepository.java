package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.UsuarioJugadorToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioJugadorTokenRepository {
    UsuarioJugadorToken save(UsuarioJugadorToken token);
    Optional<UsuarioJugadorToken> findByUsuarioIdAndJugadorId(UUID usuarioId, UUID jugadorId);
    List<UsuarioJugadorToken> findAllByUsuarioId(UUID usuarioId);
    Long count();
    void deleteAll();
}
