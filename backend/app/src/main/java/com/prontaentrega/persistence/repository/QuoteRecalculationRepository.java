package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.DatosValoracion;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Persistencia de proyecciones y actualización masiva de cotizaciones. */
public interface QuoteRecalculationRepository {
    /** Recupera una página ordenada por UUID después del cursor indicado. */
    List<DatosValoracion> buscarPagina(UUID cursorExclusivo, int limite);

    /** Actualiza por lote las cotizaciones calculadas por el dominio. */
    int actualizarCotizaciones(List<ActualizacionCotizacion> actualizaciones);

    /** Pareja ID/cotización usada únicamente para persistir el resultado ya validado. */
    record ActualizacionCotizacion(UUID bolsaId, BigDecimal cotizacionCalculada) {
    }
}