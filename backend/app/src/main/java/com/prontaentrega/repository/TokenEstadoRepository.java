package com.prontaentrega.repository;

import com.prontaentrega.models.TokenEstado;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenEstadoRepository extends JpaRepository<TokenEstado, UUID> {
    Optional<TokenEstado> findByUsuarioId(UUID usuarioId);
}
