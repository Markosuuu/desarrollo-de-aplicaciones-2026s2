package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.Usuario;
import com.prontaentrega.persistence.dto.UsuarioDTOSQL;
import com.prontaentrega.persistence.repository.UsuarioRepository;
import com.prontaentrega.persistence.repository.dao.UsuarioDAOSQL;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {
    private final UsuarioDAOSQL daosql;

    public UsuarioRepositoryImpl(UsuarioDAOSQL daosql) {
        this.daosql = daosql;
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioDTOSQL usuarioDTO = UsuarioDTOSQL.desdeUsuario(usuario);
        UsuarioDTOSQL usuarioGuardado = daosql.save(usuarioDTO);
        usuario.setId(usuarioGuardado.getId());
        return usuario;
    }

    @Override
    public void deleteAll() {
        daosql.deleteAll();
    }

    @Override
    public Optional<Usuario> findByCorreoIgnoreCase(String correo) {
        return daosql.findUsuarioDTOSQLSByCorreoIgnoreCase(correo)
                .map(UsuarioDTOSQL::aUsuario);
    }

    @Override
    public boolean existsByCorreoIgnoreCase(String correo) {
        return daosql.existsUsuarioDTOSQLByCorreoIgnoreCase(correo);
    }

    @Override
    public boolean existsById(UUID id) {
        return daosql.existsById(id);
    }

    @Override
    public Long count() {
        return daosql.count();
    }
}
