package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.UsuarioJugadorToken;
import com.prontaentrega.persistence.dto.UsuarioJugadorTokenDTOSQL;
import com.prontaentrega.persistence.repository.UsuarioJugadorTokenRepository;
import com.prontaentrega.persistence.repository.dao.UsuarioJugadorTokenDAOSQL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioJugadorTokenRepositoryImpl implements UsuarioJugadorTokenRepository {
    private final UsuarioJugadorTokenDAOSQL dao;

    public UsuarioJugadorTokenRepositoryImpl(UsuarioJugadorTokenDAOSQL dao) {
        this.dao = dao;
    }

    @Override
    public UsuarioJugadorToken save(UsuarioJugadorToken token) {
        UsuarioJugadorTokenDTOSQL dto = UsuarioJugadorTokenDTOSQL.desdeModelo(token);
        UsuarioJugadorTokenDTOSQL saved = dao.save(dto);
        token.setId(saved.getId());
        return token;
    }

    @Override
    public Optional<UsuarioJugadorToken> findByUsuarioIdAndJugadorId(UUID usuarioId, UUID jugadorId) {
        return dao.findByUsuarioIdAndJugadorId(usuarioId, jugadorId)
                .map(UsuarioJugadorTokenDTOSQL::aModelo);
    }

    @Override
    public List<UsuarioJugadorToken> findAllByUsuarioId(UUID usuarioId) {
        return dao.findAllByUsuarioId(usuarioId).stream()
                .map(UsuarioJugadorTokenDTOSQL::aModelo)
                .toList();
    }

    @Override
    public Long count() {
        return dao.count();
    }

    @Override
    public void deleteAll() {
        dao.deleteAll();
    }
}
