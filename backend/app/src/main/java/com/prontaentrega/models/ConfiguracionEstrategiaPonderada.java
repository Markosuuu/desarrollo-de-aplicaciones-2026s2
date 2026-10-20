package com.prontaentrega.models;

import java.math.BigDecimal;

/** Coeficientes y escala de la estrategia ponderada sin penalizaciones. */
public record ConfiguracionEstrategiaPonderada(
        BigDecimal pesoGoles,
        BigDecimal pesoAsistencias,
        BigDecimal pesoDisparosPorPartido,
        BigDecimal pesoPasesClavePorPartido,
        BigDecimal pesoRegatesGanadosPorPartido,
        BigDecimal pesoFaltasCometidasPorPartido,
        BigDecimal pesoRating,
        BigDecimal factorEscala) implements ConfiguracionEstrategiaCotizacion {

    /** Valida coeficientes completos, no negativos y de suma unitaria. */
    public ConfiguracionEstrategiaPonderada {
        BigDecimal[] pesos = {pesoGoles, pesoAsistencias, pesoDisparosPorPartido,
                pesoPasesClavePorPartido, pesoRegatesGanadosPorPartido,
                pesoFaltasCometidasPorPartido, pesoRating};
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
        if (factorEscala == null) {
            throw new IllegalArgumentException("El factor de escala es obligatorio");
        }
    }
}