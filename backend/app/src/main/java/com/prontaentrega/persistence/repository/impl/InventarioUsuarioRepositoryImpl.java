package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.mercado.InventarioUsuario;
import com.prontaentrega.persistence.dto.mercado.InventarioUsuarioDTOSQL;
import com.prontaentrega.persistence.dto.mercado.JugadorResumenInventarioProjection;
import com.prontaentrega.persistence.dto.mercado.JugadorResumenValorInventarioProjection;
import com.prontaentrega.persistence.repository.InventarioUsuarioRepository;
import com.prontaentrega.persistence.repository.dao.InventarioUsuarioDAOSQL;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                .map(InventarioUsuarioDTOSQL::aModelo);
    }

    @Override
    public Page<InventarioUsuario> findAllByUsuarioId(UUID usuarioId, Pageable pageable) {
        return dao.findAllByUsuarioId(usuarioId, pageable)
                .map(InventarioUsuarioDTOSQL::aModelo);
    }

    @Override
    public Long count() {
        return dao.count();
    }

    @Override
    public void deleteAll() {
        dao.deleteAll();
    }

    @Override
    public List<JugadorResumenInventarioProjection> obtenerResumenTokensPorJugador(
            UUID usuarioId) {
        return dao.obtenerResumenTokensPorJugador(usuarioId);
    }

    @Override
    public Long obtenerTotalTokens(UUID usuarioId) {
        return dao.obtenerTotalTokens(usuarioId);
    }

    public List<JugadorResumenValorInventarioProjection> obtenerValorPorJugador(
            UUID usuarioId
    ) {
        return dao.obtenerValorPorJugador(usuarioId);
    }

    public BigDecimal obtenerValorTotal(UUID usuarioId) {
        return dao.obtenerValorTotal(usuarioId);
    }
}
