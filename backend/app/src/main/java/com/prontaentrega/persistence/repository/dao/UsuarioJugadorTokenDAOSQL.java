package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.UsuarioJugadorTokenDTOSQL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJugadorTokenDAOSQL extends JpaRepository<UsuarioJugadorTokenDTOSQL, UUID> {
    Optional<UsuarioJugadorTokenDTOSQL> findByUsuarioIdAndJugadorId(UUID usuarioId, UUID jugadorId);

    List<UsuarioJugadorTokenDTOSQL> findAllByUsuarioId(UUID usuarioId);
}
