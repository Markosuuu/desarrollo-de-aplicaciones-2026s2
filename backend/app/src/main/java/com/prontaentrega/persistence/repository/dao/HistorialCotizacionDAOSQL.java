package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.models.HistorialCotizacion;
import com.prontaentrega.persistence.dto.HistorialCotizacionDTOSQL;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Acceso relacional al historial de cotizaciones. */
public interface HistorialCotizacionDAOSQL extends JpaRepository<HistorialCotizacionDTOSQL, UUID> {
    List<HistorialCotizacionDTOSQL> findByBolsaDeJugador_IdOrderByFechaAsc(UUID bolsaId);

    Optional<HistorialCotizacionDTOSQL> findFirstByBolsaDeJugador_IdAndFechaIsLessThanOrderByFechaDesc(UUID id, LocalDateTime inicioDia);

    Optional<HistorialCotizacionDTOSQL> findFirstByBolsaDeJugador_IdAndFechaIsGreaterThanEqualOrderByFechaAsc(UUID bolsaDeJugadorId, LocalDateTime fechaIsLessThan);
}