package com.prontaentrega.models;

import java.math.BigDecimal;
import java.util.UUID;

/** Proyección escalar de los datos necesarios para valorar una bolsa. */
public record DatosValoracion(
        UUID bolsaId,
        BigDecimal cotizacionVigente,
        Integer goles,
        Integer asistencias,
        BigDecimal disparosPorPartido,
        BigDecimal keyPassesPorPartido,
        BigDecimal dribblesGanadosPorPartido,
        BigDecimal faltasCometidasPorPartido,
        BigDecimal rating) {
}