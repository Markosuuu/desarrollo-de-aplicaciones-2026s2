package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.HistorialCotizacionDTOSQL;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Acceso relacional al historial de cotizaciones. */
public interface HistorialCotizacionDAOSQL extends JpaRepository<HistorialCotizacionDTOSQL, UUID> {
    List<HistorialCotizacionDTOSQL> findByBolsaDeJugador_IdOrderByFechaAsc(UUID bolsaId);
}