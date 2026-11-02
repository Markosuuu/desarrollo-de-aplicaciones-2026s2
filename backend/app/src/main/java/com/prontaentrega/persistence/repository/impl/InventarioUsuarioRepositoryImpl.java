package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.InventarioUsuario;
import com.prontaentrega.models.Usuario;
import com.prontaentrega.persistence.dto.InventarioUsuarioDTOSQL;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.persistence.repository.InventarioUsuarioRepository;
import com.prontaentrega.persistence.repository.UsuarioRepository;
import com.prontaentrega.persistence.repository.dao.InventarioUsuarioDAOSQL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class InventarioUsuarioRepositoryImpl implements InventarioUsuarioRepository {
    private final InventarioUsuarioDAOSQL dao;
    private final UsuarioRepository usuarioRepository;
    private final JugadorRepository jugadorRepository;

    public InventarioUsuarioRepositoryImpl(
            InventarioUsuarioDAOSQL dao,
            UsuarioRepository usuarioRepository,
            JugadorRepository jugadorRepository) {
        this.dao = dao;
        this.usuarioRepository = usuarioRepository;
        this.jugadorRepository = jugadorRepository;
    }

    @Override
    public InventarioUsuario save(InventarioUsuario token) {
        InventarioUsuarioDTOSQL dto = InventarioUsuarioDTOSQL.desdeModelo(token);
        InventarioUsuarioDTOSQL saved = dao.save(dto);
        token.setId(saved.getId());
        return token;
    }

    @Override
    public Optional<InventarioUsuario> findByUsuarioIdAndJugadorId(UUID usuarioId, UUID jugadorId) {
        return dao.findByUsuarioIdAndJugadorId(usuarioId, jugadorId)
                .map(this::aModelo);
    }

    @Override
    public List<InventarioUsuario> findAllByUsuarioId(UUID usuarioId) {
        return dao.findAllByUsuarioId(usuarioId).stream()
                .map(this::aModelo)
                .toList();
    }

    private InventarioUsuario aModelo(InventarioUsuarioDTOSQL dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el usuario " + dto.getUsuarioId() + " del inventario " + dto.getId()));
        BolsaDeJugador bolsaDeJugador = jugadorRepository.findById(dto.getJugadorId());
        if (bolsaDeJugador == null) {
            throw new IllegalStateException(
                    "No existe la bolsa del jugador " + dto.getJugadorId()
                            + " del inventario " + dto.getId());
        }
        return dto.aModelo(usuario, bolsaDeJugador);
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
