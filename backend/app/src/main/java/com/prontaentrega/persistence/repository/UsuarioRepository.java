package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    Usuario save(Usuario usuario);
    void deleteAll();
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsById(UUID id);
    Long count();
}
