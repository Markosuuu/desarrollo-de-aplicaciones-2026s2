package com.prontaentrega.models.estrategias;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.models.DatosValoracion;
import java.math.BigDecimal;

/** Contrato común de algoritmos de valoración sin dependencias de persistencia. */
public interface EstrategiaCotizacion {
    /** Devuelve el identificador estable usado para resolver esta estrategia. */
    String estrategiaId();

    /** Devuelve el nombre visible de la estrategia. */
    String nombreEstrategia();

    /** Declara el tipo de configuración requerido por este algoritmo. */
    Class<? extends ConfiguracionEstrategiaCotizacion> tipoConfiguracion();

    /** Calcula una cotización propuesta desde una proyección y su configuración tipada. */
    BigDecimal calcular(DatosValoracion datos, ConfiguracionEstrategiaCotizacion configuracion);
}