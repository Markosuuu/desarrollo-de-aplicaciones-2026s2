package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.cotizacion.HistorialCotizacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HistorialCotizacionRepository {
    void guardarHistorial(List<HistorialCotizacion> actualizaciones);

    List<HistorialCotizacion> buscarHistorialById(UUID jugadorId);
    void deleteAll();

    Optional<HistorialCotizacion> findFirstByBolsaIdAndFechaLessThanOrderByFechaDesc(UUID id, LocalDateTime inicioDia);

    Optional<HistorialCotizacion> findFirstByBolsaIdAndFechaGreaterThanEqualOrderByFechaAsc(UUID id, LocalDateTime inicioDia);
}
