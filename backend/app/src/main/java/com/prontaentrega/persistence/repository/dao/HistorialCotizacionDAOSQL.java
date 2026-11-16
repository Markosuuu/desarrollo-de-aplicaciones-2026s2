package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.cotizacion.HistorialCotizacionDTOSQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HistorialCotizacionDAOSQL extends JpaRepository<HistorialCotizacionDTOSQL, UUID> {
    Optional<HistorialCotizacionDTOSQL> findFirstByBolsaDeJugador_IdAndFechaIsLessThanOrderByFechaDesc(UUID id, LocalDateTime inicioDia);

    Optional<HistorialCotizacionDTOSQL> findFirstByBolsaDeJugador_IdAndFechaIsGreaterThanEqualOrderByFechaAsc(UUID bolsaDeJugadorId, LocalDateTime fechaIsLessThan);

    @Query("""
    SELECT h
    FROM HistorialCotizacion h
    WHERE h.bolsaDeJugador.estadisticasDeportivas.jugador.id = :jugadorId
    ORDER BY h.fecha asc
    """)
    List<HistorialCotizacionDTOSQL> buscarHistorialById(@Param("jugadorId") UUID jugadorId);
}