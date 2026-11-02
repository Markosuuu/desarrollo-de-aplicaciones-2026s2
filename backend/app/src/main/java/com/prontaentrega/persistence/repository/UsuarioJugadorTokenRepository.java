package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.InventarioUsuario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioJugadorTokenRepository {
    InventarioUsuario save(InventarioUsuario token);
    Optional<InventarioUsuario> findByUsuarioIdAndJugadorId(UUID usuarioId, UUID jugadorId);
    List<InventarioUsuario> findAllByUsuarioId(UUID usuarioId);
    Long count();
    void deleteAll();
}
