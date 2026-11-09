package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.InventarioUsuarioDTOSQL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioUsuarioDAOSQL extends JpaRepository<InventarioUsuarioDTOSQL, UUID> {
    @Query("""
            select inventario
            from InventarioUsuario inventario
            where inventario.usuario.id = :usuarioId
              and inventario.bolsaDeJugador.estadisticasDeportivas.jugador.id = :jugadorId
            """)
    Optional<InventarioUsuarioDTOSQL> findByUsuarioIdAndJugadorId(
            @Param("usuarioId") UUID usuarioId,
            @Param("jugadorId") UUID jugadorId);

    @Query("select inventario from InventarioUsuario inventario where inventario.usuario.id = :usuarioId")
    List<InventarioUsuarioDTOSQL> findAllByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("select inventario from InventarioUsuario inventario where inventario.usuario.id = :usuarioId")
    Page<InventarioUsuarioDTOSQL> findAllByUsuarioId(@Param("usuarioId") UUID usuarioId, Pageable pageable);
}
