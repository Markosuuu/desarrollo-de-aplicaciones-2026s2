package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.InventarioUsuario;
import com.prontaentrega.persistence.dto.InventarioUsuarioDTOSQL;
import com.prontaentrega.persistence.dto.BolsaDeJugadorDTOSQL;
import com.prontaentrega.persistence.dto.UsuarioDTOSQL;
import com.prontaentrega.persistence.repository.InventarioUsuarioRepository;
import com.prontaentrega.persistence.repository.dao.InventarioUsuarioDAOSQL;
import com.prontaentrega.persistence.repository.dao.JugadorDAOSQL;
import com.prontaentrega.persistence.repository.dao.UsuarioDAOSQL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class InventarioUsuarioRepositoryImpl implements InventarioUsuarioRepository {
    private final InventarioUsuarioDAOSQL dao;
    private final UsuarioDAOSQL usuarioDao;
    private final JugadorDAOSQL jugadorDao;

    public InventarioUsuarioRepositoryImpl(
            InventarioUsuarioDAOSQL dao,
            UsuarioDAOSQL usuarioDao,
            JugadorDAOSQL jugadorDao) {
        this.dao = dao;
        this.usuarioDao = usuarioDao;
        this.jugadorDao = jugadorDao;
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
