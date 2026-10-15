package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.TokenEstadoDTOSQL;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenEstadoDAOSQL extends JpaRepository<TokenEstadoDTOSQL, UUID> {

    Optional<TokenEstadoDTOSQL> findTokenEstadoDTOSQLByUsuarioId(UUID usuarioId);
}
