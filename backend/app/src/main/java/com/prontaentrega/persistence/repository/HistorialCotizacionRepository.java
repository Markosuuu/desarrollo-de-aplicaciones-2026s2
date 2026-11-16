package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.HistorialCotizacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HistorialCotizacionRepository {
    void guardarHistorial(List<HistorialCotizacion> actualizaciones);

    List<HistorialCotizacion> buscarPorBolsaId(UUID bolsaId);
    void deleteAll();

    Optional<HistorialCotizacion> findFirstByBolsaIdAndFechaLessThanOrderByFechaDesc(UUID id, LocalDateTime inicioDia);

    Optional<HistorialCotizacion> findFirstByBolsaIdAndFechaGreaterThanEqualOrderByFechaAsc(UUID id, LocalDateTime inicioDia);
}
