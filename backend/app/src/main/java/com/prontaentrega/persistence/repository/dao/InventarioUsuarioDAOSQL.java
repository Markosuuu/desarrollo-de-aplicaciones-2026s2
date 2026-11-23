package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.mercado.InventarioUsuarioDTOSQL;
import com.prontaentrega.persistence.dto.mercado.JugadorResumenInventarioProjection;
import com.prontaentrega.persistence.dto.mercado.JugadorResumenValorInventarioProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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
    Page<InventarioUsuarioDTOSQL> findAllByUsuarioId(@Param("usuarioId") UUID usuarioId, Pageable pageable);

    @Query("""
        select
            jugador.id as jugadorId,
            jugador.nombre as jugadorNombre,
            sum(inventario.tokensDisponibles + inventario.tokensEnVenta)
                as cantidadTokens
        from InventarioUsuario inventario
        join inventario.bolsaDeJugador.estadisticasDeportivas.jugador jugador
        where inventario.usuario.id = :usuarioId
        group by jugador.id, jugador.nombre
        order by sum(inventario.tokensDisponibles + inventario.tokensEnVenta) desc
        """)
    List<JugadorResumenInventarioProjection> obtenerResumenTokensPorJugador(
            @Param("usuarioId") UUID usuarioId
    );

    @Query("""
        select coalesce(
            sum(inventario.tokensDisponibles + inventario.tokensEnVenta),
            0
        )
        from InventarioUsuario inventario
        where inventario.usuario.id = :usuarioId
        """)
    Long obtenerTotalTokens(
            @Param("usuarioId") UUID usuarioId
    );

    @Query("""
        select
            jugador.id as jugadorId,
            jugador.nombre as jugadorNombre,
            sum(inventario.tokensDisponibles + inventario.tokensEnVenta)
                as cantidad,
            sum(
                (inventario.tokensDisponibles + inventario.tokensEnVenta)
                * inventario.bolsaDeJugador.cotizacionActual
            ) as valor
        from InventarioUsuario inventario
        join inventario.bolsaDeJugador.estadisticasDeportivas.jugador jugador
        where inventario.usuario.id = :usuarioId
        group by jugador.id, jugador.nombre
        order by valor desc
        """)
    List<JugadorResumenValorInventarioProjection> obtenerValorPorJugador(
            UUID usuarioId
    );

    @Query("""
        select sum(
            (inventario.tokensDisponibles + inventario.tokensEnVenta)
            * inventario.bolsaDeJugador.cotizacionActual
        )
        from InventarioUsuario inventario
        where inventario.usuario.id = :usuarioId
        """)
    BigDecimal obtenerValorTotal(UUID usuarioId);
}
