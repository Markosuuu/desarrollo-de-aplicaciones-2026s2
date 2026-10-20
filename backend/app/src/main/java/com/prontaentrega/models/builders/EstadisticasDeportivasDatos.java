package com.prontaentrega.models.builders;

import com.prontaentrega.models.Jugador;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EstadisticasDeportivasDatos {
    private final Jugador jugador;
    private final Boolean activo;
    private final Integer goles;
    private final Integer asistencias;
    private final BigDecimal disparosPorPartido;
    private final BigDecimal keyPassesPorPartido;
    private final BigDecimal dribblesGanadosPorPartido;
    private final BigDecimal faltasCometidasPorPartido;
    private final BigDecimal rating;
    private final Integer minutosJugados;
}