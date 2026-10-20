package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import java.util.Optional;

/** Adaptador tipado de persistencia para la configuración de una estrategia registrada. */
public interface ConfiguracionEstrategiaProvider {
    /** Identificador de la estrategia cuya configuración lee y guarda este proveedor. */
    String estrategiaId();

    /** Recupera la configuración persistida, si ya fue aprovisionada. */
    Optional<ConfiguracionEstrategiaCotizacion> buscar();

    /** Devuelve el tipo de configuración que el proveedor puede persistir. */
    Class<? extends ConfiguracionEstrategiaCotizacion> tipoConfiguracion();

    /** Persiste una configuración del tipo propio de este proveedor. */
    void guardar(ConfiguracionEstrategiaCotizacion configuracion);
}