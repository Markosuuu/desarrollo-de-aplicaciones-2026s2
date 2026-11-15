package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.mercado.InventarioUsuario;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventarioUsuarioRepository {
    InventarioUsuario save(InventarioUsuario token);
    Optional<InventarioUsuario> findByUsuarioIdAndJugadorId(UUID usuarioId, UUID jugadorId);
    Page<InventarioUsuario> findAllByUsuarioId(UUID usuarioId, Pageable pageable);
    Long count();
    void deleteAll();
}
