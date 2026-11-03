package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.HistorialCotizacion;

import java.util.List;
import java.util.UUID;

public interface HistorialCotizacionRepository {
    void guardarHistorial(List<HistorialCotizacion> actualizaciones);

    List<HistorialCotizacion> buscarPorBolsaId(UUID bolsaId);
}
