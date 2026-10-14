package com.prontaentrega.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BolsaDeJugador {
    private UUID id;
    private EstadisticasDeportivas estadisticasDeportivas;
    private BigDecimal cotizacionActual;
    private LocalDateTime fechaActualizacion;
    private String fuente;
    private Integer tokensRestantes;

    public BolsaDeJugador(
            EstadisticasDeportivas estadisticasDeportivas,
            BigDecimal cotizacionActual,
            LocalDateTime fechaActualizacion,
            String fuente,
            Integer tokensRestantes) {
        this.estadisticasDeportivas = Objects.requireNonNull(
                estadisticasDeportivas, "Las estadisticas deportivas son obligatorias");
        this.cotizacionActual = Objects.requireNonNull(
                cotizacionActual, "La cotizacion es obligatoria");
        this.fechaActualizacion = Objects.requireNonNull(
                fechaActualizacion, "La fecha de actualizacion es obligatoria");
        this.fuente = validarTexto(fuente, "La fuente es obligatoria");
        this.tokensRestantes = Objects.requireNonNull(
                tokensRestantes, "Los tokens restantes son obligatorios");
    }

    public static BolsaDeJugador desdeEstadisticas(
            Integer whoscoredId,
            String nombre,
            String equipo,
            Liga liga,
            Integer edad,
            Integer altura,
            Integer peso,
            Posicion posicion,
            Boolean activo,
            Integer goles,
            Integer asistencias,
            BigDecimal disparosPorPartido,
            BigDecimal keyPassesPorPartido,
            BigDecimal dribblesGanadosPorPartido,
            BigDecimal faltasCometidasPorPartido,
            BigDecimal rating,
            Integer minutosJugados,
            LocalDateTime fecha) {
        Jugador jugador = new Jugador(
                whoscoredId, nombre, equipo, liga, posicion, edad, altura, peso);
        EstadisticasDeportivas estadisticas = new EstadisticasDeportivas(
                jugador, activo, goles, asistencias, disparosPorPartido, null,
                keyPassesPorPartido, dribblesGanadosPorPartido,
                faltasCometidasPorPartido, rating, minutosJugados);
        return new BolsaDeJugador(
                estadisticas, BigDecimal.ONE, fecha, "WhoScored", 100);
    }

    public void actualizarEstadisticas(
            Integer edad,
            Integer altura,
            Integer peso,
            Posicion posicion,
            Boolean activo,
            Integer goles,
            Integer asistencias,
            BigDecimal disparosPorPartido,
            BigDecimal keyPassesPorPartido,
            BigDecimal dribblesGanadosPorPartido,
            BigDecimal faltasCometidasPorPartido,
            BigDecimal rating,
            Integer minutosJugados,
            LocalDateTime fecha,
            String fuente) {
        Jugador jugador = estadisticasDeportivas.getJugador();
        jugador.setEdad(edad);
        jugador.setAltura(altura);
        jugador.setPeso(peso);
        jugador.setPosicion(posicion);
        estadisticasDeportivas.actualizar(
                activo, goles, asistencias,
                disparosPorPartido, keyPassesPorPartido, dribblesGanadosPorPartido,
                faltasCometidasPorPartido, rating, minutosJugados);
        this.fechaActualizacion = Objects.requireNonNull(
                fecha, "La fecha de actualizacion es obligatoria");
        this.fuente = validarTexto(fuente, "La fuente es obligatoria");
    }

    private static String validarTexto(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
