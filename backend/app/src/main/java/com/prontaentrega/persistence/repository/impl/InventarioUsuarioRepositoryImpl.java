package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.InventarioUsuario;
import com.prontaentrega.persistence.dto.InventarioUsuarioDTOSQL;
import com.prontaentrega.persistence.repository.InventarioUsuarioRepository;
import com.prontaentrega.persistence.repository.dao.InventarioUsuarioDAOSQL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class InventarioUsuarioRepositoryImpl implements InventarioUsuarioRepository {
    private final InventarioUsuarioDAOSQL dao;

    public InventarioUsuarioRepositoryImpl(
            InventarioUsuarioDAOSQL dao) {
        this.dao = dao;
    }

    @Override
    public InventarioUsuario save(InventarioUsuario inventario) {
        InventarioUsuarioDTOSQL dto = InventarioUsuarioDTOSQL.desdeModelo(inventario);
        InventarioUsuarioDTOSQL saved = dao.save(dto);
        inventario.setId(saved.getId());
        return inventario;
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
        return dto.aModelo();
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
