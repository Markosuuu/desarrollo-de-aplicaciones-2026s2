package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.models.EstadoEstrategiaCotizacion;
import java.util.Optional;
import java.util.Set;

/** Frontera de persistencia para selección activa y configuraciones de estrategia. */
public interface QuoteStrategyRepository {
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
}