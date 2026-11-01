package com.prontaentrega.models;

import java.math.BigDecimal;

/**
 * Interfaz que define una estrategia para calcular su cotización
 */
public interface EstrategiaCotizacion {

    /**
     * Getter para el identificador de la estrategia.
     * @return El ID de la estrategia como String.
     */
    String estrategiaId();

    /**
     * Getter para el nombre de la estrategia.
     * @return El nombre de la estrategia.
     */
    String nombreEstrategia();

    /**
     * Retorna la clase de configuración específica que esta estrategia utiliza.
     * @return Una configuración de estrategia de cotización
     */
    Class<? extends ConfiguracionEstrategiaCotizacion> tipoConfiguracion();

    /** Calcula una cotización propuesta desde una proyección y su configuración tipada. */

    /**
     * Calcula la cotización con unos datos de valoracion y una configuración de estrategia
     * @param datos Proyección de datos de valoración del jugador.
     * @param configuracion Configuración específica de la estrategia de cotización.
     * @return La cotización calculada.
     */
    BigDecimal calcular(DatosValoracion datos, ConfiguracionEstrategiaCotizacion configuracion);
}