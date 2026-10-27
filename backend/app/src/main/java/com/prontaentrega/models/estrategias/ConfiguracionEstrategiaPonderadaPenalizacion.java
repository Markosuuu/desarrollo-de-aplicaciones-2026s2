package com.prontaentrega.models.estrategias;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;

import java.math.BigDecimal;

/** Pesos, umbral de regates y escala de la estrategia con penalización. */
public record ConfiguracionEstrategiaPonderadaPenalizacion(
        BigDecimal pesoGoles,
        BigDecimal pesoAsistencias,
        BigDecimal pesoDisparosPorPartido,
        BigDecimal pesoRating,
        BigDecimal pesoFaltasCometidasPorPartido,
        BigDecimal umbralRegatesGanadosPorPartido,
        BigDecimal factorEscala) implements ConfiguracionEstrategiaCotizacion {

    /** Valida los cinco coeficientes y exige los parámetros independientes. */
    public ConfiguracionEstrategiaPonderadaPenalizacion {
        BigDecimal[] pesos = {pesoGoles, pesoAsistencias, pesoDisparosPorPartido,
                pesoRating, pesoFaltasCometidasPorPartido};
        BigDecimal suma = BigDecimal.ZERO;
        for (BigDecimal peso : pesos) {
            if (peso == null || peso.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Los pesos deben estar completos y no pueden ser negativos");
            }
            suma = suma.add(peso);
        }
        if (suma.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException("La suma de los pesos debe ser igual a 1");
        }
        if (umbralRegatesGanadosPorPartido == null || factorEscala == null) {
            throw new IllegalArgumentException(
                    "El umbral de regates y el factor de escala son obligatorios");
        }
    }
}