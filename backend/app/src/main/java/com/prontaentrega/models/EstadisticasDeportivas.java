package com.prontaentrega.models;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EstadisticasDeportivas {
    private UUID id;
    private Jugador jugador;
    private Boolean activo;
    private Integer goles;
    private Integer asistencias;
    private BigDecimal disparosPorPartido;
    private BigDecimal porcentajePasesExitosos;
    private BigDecimal keyPassesPorPartido;
    private BigDecimal dribblesGanadosPorPartido;
    private BigDecimal faltasCometidasPorPartido;
    private BigDecimal rating;
    private Integer minutosJugados;

    public EstadisticasDeportivas(
            Jugador jugador,
            Boolean activo,
            Integer goles,
            Integer asistencias,
            BigDecimal disparosPorPartido,
            BigDecimal porcentajePasesExitosos,
            BigDecimal keyPassesPorPartido,
            BigDecimal dribblesGanadosPorPartido,
            BigDecimal faltasCometidasPorPartido,
            BigDecimal rating,
            Integer minutosJugados) {
        // TODO: Mover a otro método las verificaciones
        this.jugador = Objects.requireNonNull(jugador, "El jugador asociado es obligatorio");
        validarNoNegativo(goles, "Los goles no pueden ser negativos");
        validarNoNegativo(asistencias, "Las asistencias no pueden ser negativas");
        validarNoNegativo(disparosPorPartido, "Los disparos por partido no pueden ser negativos");
        validarNoNegativo(porcentajePasesExitosos, "El porcentaje de pases no puede ser negativo");
        validarNoNegativo(keyPassesPorPartido, "Los key passes por partido no pueden ser negativos");
        validarNoNegativo(dribblesGanadosPorPartido, "Los dribbles ganados por partido no pueden ser negativos");
        validarNoNegativo(faltasCometidasPorPartido, "Las faltas cometidas por partido no pueden ser negativas");
        validarNoNegativo(rating, "El rating no puede ser negativo");
        validarNoNegativo(minutosJugados, "Los minutos jugados no pueden ser negativos");
        this.activo = activo;
        this.goles = goles;
        this.asistencias = asistencias;
        this.disparosPorPartido = disparosPorPartido;
        this.porcentajePasesExitosos = porcentajePasesExitosos;
        this.keyPassesPorPartido = keyPassesPorPartido;
        this.dribblesGanadosPorPartido = dribblesGanadosPorPartido;
        this.faltasCometidasPorPartido = faltasCometidasPorPartido;
        this.rating = rating;
        this.minutosJugados = minutosJugados;
    }

    public void actualizar(
            Boolean activo,
            Integer goles,
            Integer asistencias,
            BigDecimal disparosPorPartido,
            BigDecimal keyPassesPorPartido,
            BigDecimal dribblesGanadosPorPartido,
            BigDecimal faltasCometidasPorPartido,
            BigDecimal rating,
            Integer minutosJugados) {
        this.activo = activo;
        this.goles = goles;
        this.asistencias = asistencias;
        this.disparosPorPartido = disparosPorPartido;
        this.keyPassesPorPartido = keyPassesPorPartido;
        this.dribblesGanadosPorPartido = dribblesGanadosPorPartido;
        this.faltasCometidasPorPartido = faltasCometidasPorPartido;
        this.rating = rating;
        this.minutosJugados = minutosJugados;
    }

    private static void validarNoNegativo(Integer value, String message) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void validarNoNegativo(BigDecimal value, String message) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
