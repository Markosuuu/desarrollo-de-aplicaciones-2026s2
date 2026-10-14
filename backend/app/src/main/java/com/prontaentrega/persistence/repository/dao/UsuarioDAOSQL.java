package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.UsuarioDTOSQL;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioDAOSQL extends JpaRepository<UsuarioDTOSQL, UUID> {

    Optional<UsuarioDTOSQL> findUsuarioDTOSQLSByCorreoIgnoreCase(String correo);

    boolean existsUsuarioDTOSQLByCorreoIgnoreCase(String correo);
}
