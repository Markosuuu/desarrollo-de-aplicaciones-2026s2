package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.InventarioUsuarioDTOSQL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioUsuarioDAOSQL extends JpaRepository<InventarioUsuarioDTOSQL, UUID> {
    Optional<InventarioUsuarioDTOSQL> findByUsuarioIdAndJugadorId(UUID usuarioId, UUID jugadorId);

    List<InventarioUsuarioDTOSQL> findAllByUsuarioId(UUID usuarioId);
}
