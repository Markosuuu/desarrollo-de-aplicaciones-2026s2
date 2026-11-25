package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.mercado.InventarioUsuario;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.prontaentrega.persistence.dto.mercado.JugadorResumenInventarioProjection;
import com.prontaentrega.persistence.dto.mercado.JugadorResumenValorInventarioProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventarioUsuarioRepository {
    InventarioUsuario save(InventarioUsuario token);
    Optional<InventarioUsuario> findByUsuarioIdAndJugadorId(UUID usuarioId, UUID jugadorId);
    Page<InventarioUsuario> findAllByUsuarioId(UUID usuarioId, Pageable pageable);
    Long count();
    void deleteAll();

    Long obtenerTotalTokens(UUID usuarioId);
    List<JugadorResumenInventarioProjection> obtenerResumenTokensPorJugador(UUID usuarioId);
    BigDecimal obtenerValorTotal(UUID usuarioId);
    List<JugadorResumenValorInventarioProjection> obtenerValorPorJugador(UUID usuarioId);
}
