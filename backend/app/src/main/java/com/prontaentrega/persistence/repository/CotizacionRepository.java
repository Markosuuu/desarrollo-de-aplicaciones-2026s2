package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.models.DatosValoracion;
import com.prontaentrega.models.EstadoEstrategiaCotizacion;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Frontera de persistencia para estrategias y operaciones de cotización. */
public interface CotizacionRepository {
    /** Recupera con lock la selección global. */
    Optional<EstadoEstrategiaCotizacion> bloquearEstado();

    /** Lee la selección global sin adquirir el lock de escritura. */
    Optional<EstadoEstrategiaCotizacion> buscarEstado();

    /** Persiste la selección global. */
    void guardarEstado(EstadoEstrategiaCotizacion estado);

    /** Busca configuración por el ID estable de estrategia. */
    Optional<ConfiguracionEstrategiaCotizacion> buscarConfiguracion(String estrategiaId);

    /** Devuelve los IDs de configuración que poseen provider registrado. */
    Set<String> estrategiaIdsConfigurables();

    /** Devuelve el tipo de configuración declarado por el provider registrado. */
    Class<? extends ConfiguracionEstrategiaCotizacion> tipoConfiguracion(String estrategiaId);

    /** Persiste configuración mediante el proveedor registrado para su ID. */
    void guardarConfiguracion(String estrategiaId, ConfiguracionEstrategiaCotizacion configuracion);

    /** Recupera una página ordenada por UUID después del cursor indicado. */
    List<DatosValoracion> buscarPagina(UUID cursorExclusivo, int limite);

    /** Actualiza por lote las cotizaciones calculadas por el dominio. */
    int actualizarCotizaciones(List<ActualizacionCotizacion> actualizaciones);

    /** Pareja ID/cotización usada únicamente para persistir el resultado ya validado. */
    record ActualizacionCotizacion(UUID bolsaId, BigDecimal cotizacionCalculada) {
    }
}